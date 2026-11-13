package com.inventory.client;

import com.inventory.DTO.ProductBatchInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductServiceClient {

    private final RestClient restClient;
    private final String baseUrl = "http://localhost:8082";

    public Map<UUID, ProductBatchInfo> getBatchProductInfo(List<UUID> productIds) {
        return restClient.post()
                .uri(baseUrl + "/api/v1/seller/products/internal/batch")
                .body(productIds)
                .retrieve()
                // Map<UUID, ProductBatchInfo> type preserve karne ke liye:
                .body(new ParameterizedTypeReference<Map<UUID, ProductBatchInfo>>() {});
    }
}