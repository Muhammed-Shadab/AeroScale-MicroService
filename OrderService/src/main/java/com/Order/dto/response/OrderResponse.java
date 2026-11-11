package com.Order.dto.response;

import com.Order.entity.OrderAddress;
import com.Order.entity.Orders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;



public record OrderResponse(
        UUID orderId,
        UUID buyerId,
        OrderAddress shippingAddress,
        BigDecimal totalAmount,
        String status,
        Instant holdExpiresAt,
        List<OrderItemResponse> items,
        Instant createdAt,
        String razorPayOrderid,
        String rzpKey
) {
    public static OrderResponse fromEntity(Orders order, String razorPayOrderid, String key, Instant endAt) {

        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(OrderItemResponse::fromEntity)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getBuyerId(),
                order.getShippingAddressSnapshot(),
                order.getTotalAmount(),
                order.getStatus().name(),
                endAt,
                itemResponses,
                order.getCreatedAt(),
                razorPayOrderid,
                key
        );

    }

    public static OrderResponse fromEntity(Orders order, String razorPayOrderid, String key, Instant endAt, List<OrderItemResponse> list) {

        return new OrderResponse(
                order.getId(),
                order.getBuyerId(),
                order.getShippingAddressSnapshot(),
                order.getTotalAmount(),
                order.getStatus().name(),
                endAt,
                list,
                order.getCreatedAt(),
                razorPayOrderid,
                key
        );

    }

}