package com.inventory.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Data transfer object representing batch product details fetched from ProductService.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductBatchInfo {
    private UUID productId;
    private UUID sellerId;
    private BigDecimal salePrice;
}
