package com.Order.Client;


import com.Order.dto.request.ReserveStock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryClient {

    private final RestClient restClient;
    private String base = "http://localhost:8080";

    public void updateStocks(ReserveStock stock) {
        restClient.post()
                .uri(base + "/api/inv/updateStock")
                .body(stock)
                .retrieve()
                .toBodilessEntity();
    }

    public void releaseStock(UUID orderId) {
        restClient.delete()
                .uri(base + "/api/inv/releaseStock/{orderId}", orderId)
                .retrieve()
                .toBodilessEntity();
    }

    public void confirmStock(UUID orderId) {
        restClient.put()
                .uri(base + "/api/inv/confirmStock/{orderId}", orderId)
                .retrieve()
                .toBodilessEntity();
    }
}
