package com.Auth.SellerModule.DTO.Response;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class OrderItemResponse {

    private UUID id;

    private UUID order;

    private UUID productId;

    private Integer quantity;

    private BigDecimal unitPriceAtPurchase;
    private BigDecimal subTotal;

    private Instant createdAt;

    private Boolean isFlashSaleProduct = false;

    private OrderItemStatus status;

    public enum OrderItemStatus {
        PENDING, CONFIRMED, CANCELLED, SHIPPED, DELIVERED
    }
}
