package com.Order.Client;

import com.Order.dto.request.ProductInfo;
import com.Order.dto.response.ProductDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductClient {

     private final RestClient restClient;
     private final String base = "http://localhost:8082";

     public Boolean IsProductExist(UUID productId) {
         return restClient.get()
                 .uri(base + "/api/v1/seller/products/IsProductExistBySellerId/{productId}", productId)
                 .retrieve()
                 .body(Boolean.class);

     }

     public Map<UUID, ProductInfo> getProductInfo(List<String> productIds) {
         return restClient.post()
                 .uri(base + "/api/v1/seller/products/productBasicInfo")
                 .body(productIds)
                 .retrieve()
                 .body(new ParameterizedTypeReference<Map<UUID, ProductInfo>>() {});
     }


}
