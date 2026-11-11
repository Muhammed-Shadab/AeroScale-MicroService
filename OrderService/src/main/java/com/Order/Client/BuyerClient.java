package com.Order.Client;


import com.Order.dto.request.AddressInfo;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BuyerClient {

    private final RestClient restClient;

    private final String base = "http://localhost:8081";


    public AddressInfo getBuyerAddressForCheckout(UUID shippingAddressId) {
        return restClient.get()
                .uri(base + "api/buyer/getAddressInfo/{address}", shippingAddressId)
                .retrieve()
                .body(AddressInfo.class);
    }
}
