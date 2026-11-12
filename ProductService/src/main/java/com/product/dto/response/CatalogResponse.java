package com.product.dto.response;


import com.product.entity.Product;
import com.product.entity.Product.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CatalogResponse(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        ProductStatus status,
        String imageUrl,
        Integer availableStock,
        Integer maxItemPerUser,
        Instant startTime,
        Instant endTime
) {
    public static CatalogResponse fromEntity(Product p, Integer availableStock) {
        return new CatalogResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getStatus(),
                p.getImageUrl(),
                availableStock,
                p.getMaxItemsPerUser(),
                p.getStartTime(),
                p.getEndTime()
        );
    }
}