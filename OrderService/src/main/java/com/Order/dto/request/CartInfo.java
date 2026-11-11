package com.Order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CartInfo {
    private UUID productId;
    private int itemCount;
    private String itemName;
    private BigDecimal pricePerItem;
    private Instant createdAt;
    private Instant updatedAt;
    private String imageUrl;
}
