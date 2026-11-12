package com.Order.service;

import com.Order.dto.request.CheckoutRequest;
import com.Order.dto.response.OrdersResponseOfBuyer;
import com.Order.dto.response.SellerOrderResponse;
import com.Order.entity.OrderItem;
import com.Order.entity.Orders;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OrderService {

    ResponseEntity<String> createOrder(UUID buyerId, CheckoutRequest request);

    void cancelOrder(UUID buyerId, UUID orderId);

    // Simulates a successful payment webhook : we do not have a payment service right now, for now we just simulate.
    void confirmOrder(UUID orderId, UUID id);

    OrdersResponseOfBuyer getOrderOfBuyer(UUID id, Pageable pageable,  Orders.OrderStatus status);

    List<SellerOrderResponse> getOrderRelatedToSeller(UUID id, int page, int size, OrderItem.OrderItemStatus status);

    ResponseEntity<?> updateOrderStatus(UUID id, UUID orderId);

    List<OrderItem> getOrdersOfThisDuration(Instant start, Instant end, UUID id);
}