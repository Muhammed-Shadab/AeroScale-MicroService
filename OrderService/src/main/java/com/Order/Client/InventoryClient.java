package com.Order.Client;


import com.Order.dto.request.ReserveStock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

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
}
