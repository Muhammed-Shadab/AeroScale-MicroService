package com.inventory.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Data transfer object representing seller revenue and fulfillment metrics update request.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSellerMetricsRequest {
    private UUID sellerId;
    private BigDecimal revenue;
    private Long fulfilledCount;
}
