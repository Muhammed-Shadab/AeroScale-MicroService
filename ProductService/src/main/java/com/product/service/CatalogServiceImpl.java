package com.product.service;


import com.product.Client.InventoryClient;
import com.product.dto.request.CatalogQuery;
import com.product.dto.response.CatalogResponse;
import com.product.entity.Product;
import com.product.entity.Product.ProductStatus;
import com.product.exception.ProductNotFoundException;
import com.product.repository.ProductRepository;
import com.product.repository.ProductSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    private final ProductRepository productRepository;
    private final InventoryClient inventoryClient;



    @Override
    @Transactional(readOnly = true)
    public Page<CatalogResponse> searchProducts(CatalogQuery query, Pageable pageable) {
        Specification<Product> spec = ProductSpecs.withStatus(query.getStatus());
        if (query.getQ() != null && !query.getQ().isBlank()) spec = spec.and(ProductSpecs.textContains(query.getQ()));
        if (query.getMinPrice() != null) spec = spec.and(ProductSpecs.priceAtLeast(query.getMinPrice()));
        if (query.getMaxPrice() != null) spec = spec.and(ProductSpecs.priceAtMost(query.getMaxPrice()));


        Page<Product> page = productRepository.findAll(spec, pageable);
        List<Product> products = page.getContent();

        Map<UUID, Integer> stock = stockFor(products);
        Instant now = Instant.now();

        return page.map(p -> CatalogResponse.fromEntity(p,
                stock.getOrDefault(p.getId(), 0)));
    }

    @Override
    @Transactional(readOnly = true)
    public CatalogResponse getActiveProductById(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        // Buyer DRAFT/ARCHIVED product nahi dekh sakta, ID pata ho tab bhi
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new ProductNotFoundException("Product not found");
        }

        List<List<Integer>> stock = inventoryClient.getTotalStockThatAdded(List.of(productId));


        return CatalogResponse.fromEntity(product, stock.get(0).get(1));
    }

//    @Override
//    @Transactional(readOnly = true)
//    public Page<CatalogResponse> getFlashSaleCatalog(SaleState state, int page, int size) {
//        int safePage = Math.max(page, 0);
//        int safeSize = Math.min(Math.max(size, 1), 50);
//
//        // LIVE: jo pehle khatam hogi wo pehle. UPCOMING: jo pehle shuru hogi wo pehle
//        Sort sort = Sort.by(state == SaleState.LIVE ? "endTime" : "startTime").ascending();
//        Page<FlashSale> sales = flashSaleService.findSalesPage(state, PageRequest.of(safePage, safeSize, sort));
//
//        List<UUID> ids = sales.getContent().stream().map(FlashSale::getProductId).toList();
//        Map<UUID, Product> products = productRepository.findAllById(ids).stream()
//                .collect(Collectors.toMap(Product::getId, Function.identity()));
//        Map<UUID, Integer> stock = stockFor(products.values());
//        Instant now = Instant.now();
//
//        List<CatalogResponse> content = new ArrayList<>();
//        for (FlashSale sale : sales.getContent()) {
//            Product p = products.get(sale.getProductId());
//            // archived/draft product ka sale card nahi dikhana
//            if (p == null || p.getStatus() != ProductStatus.ACTIVE) continue;
//            content.add(CatalogResponse.fromEntity(p, stock.getOrDefault(p.getId(), 0),
//                    FlashSaleInfo.from(sale, p.getPinventoryRepository.findByProductIdIn(ids).stream()
//                .collect(Collectors.toMap(Inventory::getProductId, Inventory::getAvailableStock));rice(), now)));
//        }
//        return new PageImpl<>(content, sales.getPageable(), sales.getTotalElements());
//    }
//
//    private FlashSaleInfo infoOrNull(FlashSale sale, Product product, Instant now) {
//        return sale == null ? null : FlashSaleInfo.from(sale, product.getPrice(), now);
//    }
//
    private Map<UUID, Integer> stockFor(List<Product> products) {
        List<UUID> idx = products.stream().map(Product::getId).toList();
        if (idx.isEmpty()) return Map.of();

        List<List<Integer>> stocks = inventoryClient.getTotalStockThatAdded(idx);

        Map<UUID, Integer> map = new HashMap<>();
        for(int i=0;i<idx.size();i++) {
            map.put(idx.get(i), stocks.get(i).get(1));
        }

        return map;
    }
}