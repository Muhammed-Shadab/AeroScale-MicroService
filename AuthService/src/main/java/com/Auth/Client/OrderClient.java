package com.Auth.Client;

import com.Auth.SellerModule.DTO.Response.OrderItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderClient {

    private final RestClient restClient;
    private String BaseUrl = "http://localhost:8080";

    public List<OrderItemResponse> getOrderItemsBetweenThisDuration(UUID id, Instant start, Instant end) {
        return restClient.get()
                .uri(BaseUrl + "/api/v1/orders/getOrderItemsFrom/{start}/to/{end}", start, end)
                .retrieve()
                .body(new ParameterizedTypeReference<List<OrderItemResponse>>() {});

    }

}
