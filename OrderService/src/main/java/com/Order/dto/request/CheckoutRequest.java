package com.Order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CheckoutRequest {

        @NotNull(message = "Shipping address ID is required to place an order")
        private UUID shippingAddressId;

        @NotBlank(message = "Idempotency key is required to prevent duplicate orders")
        private String idempotencyKey;

        private List<CartInfo> cartInfoList;

        @NotNull(message = "RecipentName Cannot be Empty")
        private String RecipientName;

        @NotNull(message = "Recipient Phone number cannot be null")
        private String RecipientPhoneNo;

        @NotNull(message = "Recipient Phone number cannot be null")
        private String AddressLine1;


        private String addressLine2;

        @NotNull(message = "Please mention the city")
        private String city;

        @NotNull(message = "Please mention the state")
        private String state;

        @NotNull(message = "Please mention the pincode")
        private String pincode;

        @NotNull(message = "Please mention the country")
        private String country;

}