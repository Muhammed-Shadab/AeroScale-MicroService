package com.Auth.BuyerModule.DTO.Request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductDetails {
    private UUID id;
    private UUID sellerId;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private int totalStock;
    private int availableStock;
}
