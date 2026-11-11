package com.Order.dto.response;

import com.Order.entity.OrderAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerOrderResponse {
        private String orderId;
        private String status;
        private OrderAddress shippingAddress;
        private ProductDetails item;
        private BigDecimal sellerTotal;
}


