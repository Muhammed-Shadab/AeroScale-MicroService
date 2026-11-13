package com.payment.DTO.Request;

import jakarta.validation.constraints.NotNull;
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
public class CreatePaymentOrderRequest {

    @NotNull
    private UUID orderId;

    @NotNull
    private UUID buyerId;

    @NotNull
    private BigDecimal amount;
}