package com.Order.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductInfo {
    private UUID productId;
    private String productName;
    private String imageUrl;
    private BigDecimal unitPrice;
}
