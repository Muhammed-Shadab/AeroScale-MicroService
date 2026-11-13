package com.payment.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;


@Component
@RequiredArgsConstructor
public class OrderServiceClient {

    private final RestClient restClient;
    private final String orderServiceBaseUrl = "http://localhost:8083";


    // Confirms the order in OrderService after payment verification.

    public void confirmOrderPayment(UUID orderId, UUID buyerId) {
        restClient.put()
                .uri(orderServiceBaseUrl + "/api/v1/orders/internal/confirm/{orderId}?buyerId={buyerId}", orderId, buyerId)
                .retrieve()
                .toBodilessEntity();
    }
}