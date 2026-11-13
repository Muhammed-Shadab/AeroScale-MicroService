package com.inventory.DTO.Request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReserveStock {

    List<ProductIdAndStockToReserv> productInfoList;
    private String orderId;
}
