package com.inventory.client;

import com.inventory.DTO.UpdateSellerMetricsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SellerServiceClient {

    private final RestClient restClient;
    private final String baseUrl = "http://localhost:8081";

    public void batchUpdateSellerMetrics(List<UpdateSellerMetricsRequest> updates) {
        restClient.put()
                .uri(baseUrl + "/api/seller/internal/batch-update-metrics")
                .body(updates)
                .retrieve()
                .toBodilessEntity();
    }
}