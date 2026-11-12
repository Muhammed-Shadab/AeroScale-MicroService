package com.product.dto.request;

import com.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CatalogQuery {
    private String q;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Product.ProductStatus status;
}


