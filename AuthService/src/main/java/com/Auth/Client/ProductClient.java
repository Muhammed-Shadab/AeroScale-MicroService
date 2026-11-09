package com.Auth.Client;

import com.Auth.BuyerModule.DTO.Request.ProductDetails;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductClient {

    private RestClient restClient;
    private String BaseUrl = "http://localhost:8082";


    public ProductDetails getProductDetails(UUID productId) {
        return restClient.get()
                .uri(BaseUrl + "/api/v1/seller/products/{productId}" + productId)
                .retrieve()
                .body(ProductDetails.class);
    }

    public boolean doestThisProductExist(UUID productId) {
        return restClient.get()
                .uri(BaseUrl + "/api/v1/seller/products/IsProductExist/{productId}", productId)
                .retrieve()
                .body(Boolean.class);
    }
}
