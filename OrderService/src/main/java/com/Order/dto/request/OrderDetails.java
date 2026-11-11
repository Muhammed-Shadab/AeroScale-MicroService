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
public class OrderDetails {

    private UUID orderId;
    private BigDecimal amount;
}
