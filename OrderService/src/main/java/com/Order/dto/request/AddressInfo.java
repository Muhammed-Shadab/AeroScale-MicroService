package com.Order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AddressInfo {
    private UUID id;
    private String recipientName;
    private String recipientPhoneNo;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String country;
    private String pincode;
    private boolean isDefault;
    private Instant createdAt;
    private Instant updatedAt;
}
