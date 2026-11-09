package com.product.Client;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryClient {

    private final RestClient restClient;
    private String BaseUrl = "http://localhost:8080";

    public void initializeInventory(UUID productId) {
        restClient.put()
                .uri(BaseUrl + "/api/inv/initializeInventory/{productId}", productId)
                .retrieve()
                .toBodilessEntity();
    }


    public List<List<Integer>> getTotalStockThatAdded(List<UUID> productId) {
        return restClient.post()
                .uri(BaseUrl + "/api/inv/getStockMetaData")
                .body(productId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<List<Integer>>>() {});

    }

    public void addStock(UUID productId, Integer quantity) {
        restClient.put()
                .uri(BaseUrl + "/api/inv/addStock/{productId}/{quantity}", productId, quantity)
                .retrieve()
                .toBodilessEntity();
    }
}
