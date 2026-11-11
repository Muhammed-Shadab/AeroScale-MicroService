package com.Order.service.impl;

import com.Order.Client.BuyerClient;
import com.Order.Client.InventoryClient;
import com.Order.Client.PaymentClient;
import com.Order.Client.ProductClient;
import com.Order.dto.request.*;
import com.Order.dto.response.*;

import com.Order.entity.OrderAddress;
import com.Order.entity.OrderItem;
import com.Order.entity.Orders;
import com.Order.exception.EmptyCartException;
import com.Order.exception.OrderNotAvailabe;
import com.Order.exception.OrderValidationException;
import com.Order.exception.ProductIsNotAvailable;
import com.Order.repository.OrderItemRepository;
import com.Order.repository.OrderRepository;
import com.Order.service.OrderService;

import io.netty.channel.AdaptiveRecvByteBufAllocator;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    @Value("${razorpay-key}")
    private String key;

    private final OrderRepository orderRepository;
//    private final FlashSaleService flashSaleService;
    private final OrderItemRepository orderItemRepository;

    private final BuyerClient buyerClient;
    private final ProductClient productClient;
    private final PaymentClient paymentClient;
    private final InventoryClient inventoryClient;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<String> createOrder(UUID buyerId, CheckoutRequest request){

        // 1. IDEMPOTENCY GUARD: Check if this checkout attempt already succeeded
        Optional<Orders> existingOrder = orderRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existingOrder.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Order Already Exist");
        }

        // 2. Fetch Cart
        List<CartInfo> cartItems = request.getCartInfoList();

        if (cartItems == null || cartItems.isEmpty()) {
            throw new EmptyCartException("Cannot place an order with an empty cart");
        }


        OrderAddress snapshotAddress = OrderAddress.builder()
                .recipientName(request.getRecipientName())
                .recipientPhoneNo(request.getRecipientPhoneNo())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .country(request.getCountry())
                .build();

        // 4. Initialize Order (Pre-generate UUID so Inventory can use it)
        UUID orderId = UUID.randomUUID();
        Orders order = Orders.builder()
                .id(orderId)
                .buyerId(buyerId)
                .idempotencyKey(request.getIdempotencyKey())
                .shippingAddressSnapshot(snapshotAddress)
                .status(Orders.OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal calculatedTotal = BigDecimal.ZERO;

        List<String> productIds = new ArrayList<>();

        List<ProductIdAndStockToReserv> list = new ArrayList<>();

        for(CartInfo cartItem: cartItems) {
            productIds.add(cartItem.getProductId().toString());

            list.add(new ProductIdAndStockToReserv(cartItem.getProductId(), cartItem.getItemCount()));
        }

        ReserveStock reserveStock = ReserveStock.builder()
                .productInfoList(list)
                .orderId(orderId.toString())
                .build();

        inventoryClient.updateStocks(reserveStock);
        Map<UUID, ProductInfo> prices = productClient.getProductInfo(productIds);


        // 5. Process Items: Fetch Price & Reserve Stock
        for (CartInfo cartItem : cartItems) {

            BigDecimal unitPrice = prices.get(cartItem.getProductId()).getUnitPrice();

            BigDecimal subTotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getItemCount()));

            calculatedTotal = calculatedTotal.add(subTotal);

            OrderItem orderItem = OrderItem.builder()
                    .productId(cartItem.getProductId())
                    .quantity(cartItem.getItemCount())
                    .unitPriceAtPurchase(unitPrice)
                    .subTotal(subTotal)
                    .isFlashSaleProduct(false)
                    .build();

            order.addOrderItem(orderItem);
        }

        order.setTotalAmount(calculatedTotal);

        // 6. Save the Order & Clear Cart
        Orders savedOrder = orderRepository.save(order);

        paymentClient.createRazorPayOrder(new OrderDetails(savedOrder.getId(), savedOrder.getTotalAmount()));
        return ResponseEntity.status(HttpStatus.CREATED).body(orderId.toString());
    }

    @Override
    @Transactional
    public void cancelOrder(UUID buyerId, UUID orderId) {
        Orders order = orderRepository.findByIdAndBuyerId(orderId, buyerId)
                .orElseThrow(() -> new RuntimeException("Order not found or access denied"));

        // We only allow cancellation if payment hasn't been confirmed yet
        if (order.getStatus() != Orders.OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be cancelled");
        }


        // Loop through the items and tell the vault to release the locks

        for (OrderItem item : order.getOrderItems()) {
            item.setStatus(OrderItem.OrderItemStatus.CANCELLED);
            inventoryService.releaseStock(orderId, item.getProductId());
        }


        // Mark as cancelled and save
        order.setStatus(Orders.OrderStatus.CANCELLED);

        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void confirmOrderPayment(UUID orderId, UUID buyerId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != Orders.OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be confirmed");
        }

        // Tell the Vault to permanently deduct the physical stock
        for (OrderItem item : order.getOrderItems()) {
            inventoryService.confirmStock(orderId, item.getProductId());
            // sale ACTIVE hai to "claimed" bar badhao
            flashSaleService.recordSale(item.getProductId(), item.getQuantity());
            item.setStatus(OrderItem.OrderItemStatus.CONFIRMED);
        }

        order.setStatus(Orders.OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }

    @Override
    public OrdersResponseOfBuyer getOrderOfBuyer(UUID id, Pageable pageable, Orders.OrderStatus status) {
        if(id == null) throw new IllegalArgumentException("login first");
        List<Orders> orderList = orderRepository.findOrdersOfBuyer(id,pageable,status);

        OrdersResponseOfBuyer response = new OrdersResponseOfBuyer();
        response.setLast(true);
        response.setTotalElements(orderList.size());
        response.setTotalPages(1);
        response.setNumber(1);
        response.setSize(orderList.size());



        if(!orderList.isEmpty()) {

            Set<String> set = new HashSet<>();
            for (Orders order : orderList) {
                for (OrderItem orderItem : order.getOrderItems()) {
                    set.add(orderItem.getProductId().toString());
                }
            }


            Map<UUID, ProductInfo> productInfoMap = productClient.getProductInfo(new ArrayList<>(set));


            List<String> orderIds = new ArrayList<>();
            for (Orders order : orderList) {
                orderIds.add(order.getId().toString());
            }
            Map<UUID, String> rzpIdMap = paymentClient.getRzrPayOrderId(orderIds);

            List<OrderResponse> contentList = new ArrayList<>();
            for (Orders order : orderList) {
                List<OrderItemResponse> orderItemResponses = new ArrayList<>();
                for (OrderItem orderItem : order.getOrderItems()) {
                    orderItemResponses.add(OrderItemResponse.fromEntity(orderItem,
                            productInfoMap.get(orderItem.getProductId()).getProductName(),
                            productInfoMap.get(orderItem.getProductId()).getImageUrl()));
                }
                contentList.add(OrderResponse.fromEntity(order, rzpIdMap.get(order.getId()), key, null, orderItemResponses));
            }

            response.setContent(contentList);
        }

        return response;
    }


    @Override
    public List<SellerOrderResponse> getOrderRelatedToSeller(UUID id, int page, int size, OrderItem.OrderItemStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OrderItem> ordersPage = orderItemRepository.findOrdersOfSeller(id, status, pageable);

        List<OrderItem> ordersList = ordersPage.get().toList();

        List<SellerOrderResponse> sellerOrderResponses = new ArrayList<>();
        for(OrderItem oi: ordersList) {
            UUID productId = oi.getProductId();

            Map<UUID, ProductInfo> productMap = productClient.getProductInfo(List.of(productId.toString()));
            ProductInfo product = productMap.get(productId);


            ProductDetails productDetails = ProductDetails.builder()
                    .productId(product.getProductId())
                    .imageUrl(product.getImageUrl())
                    .productName(product.getProductName())
                    .quantity(oi.getQuantity())
                    .unitPrice(oi.getUnitPriceAtPurchase())
                    .subTotal(oi.getSubTotal())
                    .build();


            sellerOrderResponses.add(SellerOrderResponse.builder()
                    .orderId(oi.getId().toString())
                    .status(oi.getStatus().toString())
                    .item(productDetails)
                    .shippingAddress(oi.getOrder().getShippingAddressSnapshot())
                    .sellerTotal(oi.getSubTotal())
                    .build());
        }

        return sellerOrderResponses;
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateOrderStatus(UUID id, UUID orderId) {


        OrderItem orderItem = orderItemRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotAvailabe("orderItem Record Not found!!"));

        if(!productClient.IsProductExist(orderItem.getProductId())) {
            throw new ProductIsNotAvailable("Only Owner can perform this transition!!");
        }

        if(orderItem.getStatus().toString().equals("PENDING")) throw new OrderValidationException("Order Is in Pending State!!");
        else if(orderItem.getStatus().toString().equals("CONFIRMED")) orderItem.setStatus(OrderItem.OrderItemStatus.SHIPPED);
        else if(orderItem.getStatus().toString().equals("SHIPPED")) orderItem.setStatus(OrderItem.OrderItemStatus.DELIVERED);
        else throw new OrderNotAvailabe("Order is in " + orderItem.getStatus() + "status and bcz of this the transition is not possible");


        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Override
    @Transactional
    public void createOrderForFlashSale(FlashSaleProductDetails productDetails, UUID buyerId, UUID orderId) {

        AddressInfo addressResponse = buyerClient.getBuyerAddressForCheckout(productDetails.getShippingAddressId());

        OrderAddress snapshotAddress = OrderAddress.builder()
                .recipientName(addressResponse.getRecipientName())
                .recipientPhoneNo(addressResponse.getRecipientPhoneNo())
                .addressLine1(addressResponse.getAddressLine1())
                .addressLine2(addressResponse.getAddressLine2())
                .city(addressResponse.getCity())
                .state(addressResponse.getState())
                .pincode(addressResponse.getPincode())
                .country(addressResponse.getCountry())
                .build();


        Orders order = Orders.builder()
                .id(orderId)
                .buyerId(buyerId)
                .idempotencyKey("It Is Deprecated")
                .shippingAddressSnapshot(snapshotAddress)
                .status(Orders.OrderStatus.PENDING)
                .totalAmount(productDetails.getTotalPrice())
                .build();


        OrderItem orderItem = OrderItem.builder()
                .productId(productDetails.getProductId())
                .quantity(productDetails.getQuantity())
                .unitPriceAtPurchase(productDetails.getTotalPrice().divide(BigDecimal.valueOf(productDetails.getQuantity())))
                .subTotal(productDetails.getTotalPrice())
                .isFlashSaleProduct(true)
                .build();

        orderRepository.save(order);
        orderItemRepository.save(orderItem);
    }

    @Override
    public List<OrderItem> getOrdersOfThisDuration(Instant start, Instant end, UUID id) {
        return orderItemRepository.getOrOrderItemsBetweenThisDuration(id, start, end);
    }
}
