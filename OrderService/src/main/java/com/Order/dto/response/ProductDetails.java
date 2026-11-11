package com.Order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductDetails {
    private UUID productId;
    private String productName;
    private String imageUrl;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal subTotal;
}
