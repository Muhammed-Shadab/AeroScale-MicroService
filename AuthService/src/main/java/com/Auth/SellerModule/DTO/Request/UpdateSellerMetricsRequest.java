package com.Auth.SellerModule.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSellerMetricsRequest {
    private UUID sellerId;
    private BigDecimal revenue;
    private Long fulfilledCount;
}