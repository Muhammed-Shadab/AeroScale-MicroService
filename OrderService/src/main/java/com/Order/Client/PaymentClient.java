package com.Order.Client;

import com.Order.dto.request.OrderDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentClient {

    private final RestClient restClient;
    private final String base = "http://localhost:8084";


    public Map<UUID, String> getRzrPayOrderId(List<String> orderIds) {
        return restClient.post()
                .uri(base + "/api/pay/getRzrPayOrderId")
                .body(orderIds)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<UUID, String>>() {});
    }

    public void createRazorPayOrder(OrderDetails orderDetails) {
         restClient.post()
                 .uri(base + "/api/pay/CreateRzrPayOrderId")
                 .body(orderDetails)
                 .retrieve()
                 .toBodilessEntity();
    }
}
