package com.inventory.service.impl;


import com.inventory.DTO.ProductBatchInfo;
import com.inventory.DTO.Request.ProductIdAndStockToReserv;
import com.inventory.DTO.Request.ReserveStock;
import com.inventory.DTO.UpdateSellerMetricsRequest;
import com.inventory.client.ProductServiceClient;
import com.inventory.client.SellerServiceClient;
import com.inventory.entity.Inventory;
import com.inventory.entity.InventoryReservation;
import com.inventory.exception.ProductIsNotAvailable;
import com.inventory.repository.InventoryRepository;
import com.inventory.repository.InventoryReservationRepository;
import com.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;
    private final ProductServiceClient productServiceClient;
    private final SellerServiceClient sellerServiceClient;

    // Hardcoded for now...later tp application.properties
    private static final int LOCK_TIMEOUT_MINUTES = 5;

    @Override
    @Transactional
    public void reserveStock(ReserveStock stocks) {

        if (stocks == null || stocks.getProductInfoList() == null || stocks.getProductInfoList().isEmpty()) {
            throw new IllegalArgumentException("Reservation request or product list cannot be empty");
        }

        UUID orderId = UUID.fromString(stocks.getOrderId());
        List<ProductIdAndStockToReserv> items = stocks.getProductInfoList();

        for(ProductIdAndStockToReserv item: items) {

            if (item.getStock() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero for product: " + item.getProductId());
            }
            // 1.checking if already done or not..idempotency
            Optional<InventoryReservation> existing = reservationRepository
                    .findByOrderIdAndProductId(orderId, item.getProductId());

            if (existing.isPresent()) {
                if (existing.get().getStatus() == InventoryReservation.ReservationStatus.PENDING) {
                    log.info("Stock already reserved for order: {} and product: {}", orderId, item.getProductId());
                    continue; // Already locked for this order, skip locking again
                } else {
                    throw new IllegalStateException("Reservation already exists with status: " + existing.get().getStatus());
                }
            }
            // 2. Fetch inventory
            Inventory inventory = inventoryRepository.findByProductId(item.getProductId())
                    .orElseThrow(() -> new ProductIsNotAvailable("Product not found in inventory: " + item.getProductId()));

            // 3. Availability Check
            if (inventory.getAvailableStock() < item.getStock()) {
                throw new IllegalStateException("Insufficient available stock for product: " + item.getProductId());
            }

            // 4. Lock the stock
            inventory.setLockedStock(inventory.getLockedStock() + item.getStock());

            // 5. Create reservation record
            InventoryReservation reservation = InventoryReservation.builder()
                    .productId(item.getProductId())
                    .orderId(orderId)
                    .quantityLocked(item.getStock())
                    .status(InventoryReservation.ReservationStatus.PENDING)
                    .expiresAt(Instant.now().plus(LOCK_TIMEOUT_MINUTES, ChronoUnit.MINUTES))
                    .build();

            reservationRepository.save(reservation);
        }

    }

    @Override
    @Transactional
    public void confirmStock(UUID orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("OrderId cannot be null");
        }

        // 1. Order ke saare PENDING reservations fetch
        List<InventoryReservation> reservations = reservationRepository
                .findByOrderIdAndStatus(orderId, InventoryReservation.ReservationStatus.PENDING);

        if (reservations.isEmpty()) {
            log.warn("No pending reservations found for order: {}", orderId);
            return; // Idempotent: already confirmed or expired
        }

        // 2. Saare distinct productIds
        List<UUID> productIds = reservations.stream()
                .map(InventoryReservation::getProductId)
                .distinct()
                .toList();

        // 3. Inventory records ko SINGLE query me batch fetch
        List<Inventory> inventoryList = inventoryRepository.findByProductIdIn(productIds);
        Map<UUID, Inventory> inventoryMap = inventoryList.stream()
                .collect(Collectors.toMap(Inventory::getProductId, Function.identity()));

        // 4. [HTTP CALL 1]: ProductService ko 1 batch call karke saari details
        Map<UUID, ProductBatchInfo> productInfoMap = productServiceClient.getBatchProductInfo(productIds);

        // 5. In-Memory Map: Seller ID -> Aggregated {revenue, fulfilledCount}
        // Agar order me ek hi seller ke 2 products hain toh unka total yahin jud jayega!
        Map<UUID, UpdateSellerMetricsRequest> sellerUpdateMap = new HashMap<>();

        for (InventoryReservation reservation : reservations) {
            UUID productId = reservation.getProductId();
            int quantity = reservation.getQuantityLocked();

            // Deduct stock from Inventory
            Inventory inventory = inventoryMap.get(productId);
            if (inventory != null) {
                inventory.setTotalStock(inventory.getTotalStock() - quantity);
                inventory.setLockedStock(inventory.getLockedStock() - quantity);
            }

            //Seller metrics calculate karo
            ProductBatchInfo productInfo = productInfoMap.get(productId);
            if (productInfo != null && productInfo.getSellerId() != null) {
                UUID sellerId = productInfo.getSellerId();
                BigDecimal itemTotal = productInfo.getSalePrice().multiply(BigDecimal.valueOf(quantity));

                // Seller map me aggregate (accumulate) karo
                sellerUpdateMap.compute(sellerId, (key, existing) -> {
                    if (existing == null) {
                        return UpdateSellerMetricsRequest.builder()
                                .sellerId(sellerId)
                                .revenue(itemTotal)
                                .fulfilledCount((long) quantity)
                                .build();
                    } else {
                        existing.setRevenue(existing.getRevenue().add(itemTotal));
                        existing.setFulfilledCount(existing.getFulfilledCount() + quantity);
                        return existing;
                    }
                });
            }

            //Mark reservation as CONFIRMED
            reservation.setStatus(InventoryReservation.ReservationStatus.CONFIRMED);
        }

        // 6. [HTTP CALL 2]: Seller ko 1 batch call karke sab update
        if (!sellerUpdateMap.isEmpty()) {
            List<UpdateSellerMetricsRequest> sellerUpdates = new ArrayList<>(sellerUpdateMap.values());
            sellerServiceClient.batchUpdateSellerMetrics(sellerUpdates);
        }

        log.info("Batch stock confirmation & seller update completed successfully for order: {}", orderId);
    }

    @Override
    @Transactional
    public void releaseStock(UUID orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("OrderId cannot be null");
        }

        // 1. khali pending
        List<InventoryReservation> reservations = reservationRepository
                .findByOrderIdAndStatus(orderId, InventoryReservation.ReservationStatus.PENDING);

        if (reservations.isEmpty()) {
            log.warn("No pending reservations found for cancelled order: {}", orderId);
            return; // Idempotent: already cancelled or already expired
        }

        // 2. Collect all productIds in a Set to avoid duplicates
        Set<UUID> productIds = reservations.stream()
                .map(InventoryReservation::getProductId)
                .collect(Collectors.toSet());

        // 3. Batch fetch kro Inventories in 1 single query
        List<Inventory> inventoryList = inventoryRepository.findByProductIdIn(productIds);
        Map<UUID, Inventory> inventoryMap = inventoryList.stream()
                .collect(Collectors.toMap(Inventory::getProductId, Function.identity()));

        // 4. Releasing lock foreach item
        for (InventoryReservation reservation : reservations) {
            Inventory inventory = inventoryMap.get(reservation.getProductId());
            if (inventory != null) {
                int newLockedStock = Math.max(0, inventory.getLockedStock() - reservation.getQuantityLocked());
                inventory.setLockedStock(newLockedStock);
            }

            reservation.setStatus(InventoryReservation.ReservationStatus.CANCELLED);
        }

        log.info("Successfully released stock for order: {} (Total items released: {})", orderId, reservations.size());
    }

    @Override
    @Transactional
    public void releaseExpiredReservations() {
        // Find everything that is PENDING but the timer has run out
        List<InventoryReservation> expiredLocks = reservationRepository
                .findByStatusAndExpiresAtBefore(InventoryReservation.ReservationStatus.PENDING, Instant.now());

        if (expiredLocks.isEmpty()) {
            return;
        }

        log.info("Found {} expired stock reservations. Releasing locks...", expiredLocks.size());

        for (InventoryReservation reservation : expiredLocks) {
            Inventory inventory = inventoryRepository.findByProductId(reservation.getProductId())
                    .orElseThrow(() -> new IllegalStateException("Inventory record not found"));

            // Release the lock back to the available pool
            inventory.setLockedStock(inventory.getLockedStock() - reservation.getQuantityLocked());
            reservation.setStatus(InventoryReservation.ReservationStatus.EXPIRED);
        }
    }

    @Override
    @Transactional
    public void initializeInventory(UUID productId) {
        // Prevent duplicate ledger creation
        if (inventoryRepository.findByProductId(productId).isPresent()) {
            throw new IllegalStateException("Inventory ledger already exists for this product");
        }

        Inventory newLedger = Inventory.builder()
                .productId(productId)
                .totalStock(0)
                .totalStockAdded(0)
                .lockedStock(0)
                .build();

        inventoryRepository.save(newLedger);
    }

    @Override
    @Transactional
    public void addStock(UUID productId, int additionalQuantity) {
        if (additionalQuantity <= 0) {
            throw new IllegalArgumentException("Restock quantity must be strictly greater than zero");
        }

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalStateException("Inventory ledger not found for this product"));

        // Add the new shipment to the physical total
        inventory.setTotalStock(inventory.getTotalStock() + additionalQuantity);
        inventory.setTotalStockAdded(inventory.getTotalStockAdded() + additionalQuantity);

        log.info("Restocked {} units for product {}. New Total: {}",
                additionalQuantity, productId, inventory.getTotalStock());
    }


    // Helper method
    private InventoryReservation getPendingReservationOrThrow(UUID orderId, UUID productId) {
        InventoryReservation reservation = reservationRepository.findByOrderIdAndProductId(orderId, productId)
                .orElseThrow(() -> new IllegalStateException("No active reservation found for this order"));

        if (reservation.getStatus() != InventoryReservation.ReservationStatus.PENDING) {
            throw new IllegalStateException("Reservation is not in PENDING state");
        }
        return reservation;
    }

    @Override
    public int getStock(UUID productId) {
        if(productId == null) throw new RuntimeException("Product id should Not be Null!!");

        Inventory inventory = inventoryRepository.findByProductId(productId).
                orElseThrow(() -> new ProductIsNotAvailable("Product Not Available!!"));
        return inventory.getAvailableStock();
    }

    @Override
    public List<List<Integer>> getTotalStockThatAdded(List<UUID> productIdList) {
        List<List<Integer>> res = new ArrayList<>();

        for(UUID id: productIdList) {
            Inventory inventory = inventoryRepository.findByProductId(id).
                    orElseThrow(() -> new ProductIsNotAvailable("Product Not Available!!"));

            List<Integer> row = new ArrayList<>();
            row.add(inventory.getTotalStockAdded());
            row.add(inventory.getAvailableStock());

            res.add(row);
        }
        return res;
    }



}