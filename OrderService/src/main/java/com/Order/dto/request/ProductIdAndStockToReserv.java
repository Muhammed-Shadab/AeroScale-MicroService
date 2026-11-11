package com.Order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductIdAndStockToReserv {
    @NotNull(message = "product Id Cannot Be null")
    private UUID productId;

    @Min(value = 1, message = "Product Stock Should be Greater Then zero")
    private int stock;

}
