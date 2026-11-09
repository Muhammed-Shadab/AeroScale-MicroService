package com.product.cache;

import com.product.Client.InventoryClient;
import com.product.dto.response.ProductResponse;
import com.product.entity.Product;
import com.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.sort;

@Service
@RequiredArgsConstructor
public class ProductCache {
    private static final int maxPages = 40;

    private final ProductRepository productRepository;
    private final InventoryClient inventoryClient;

    public boolean canCacheThisPage(Pageable pageable) {
        return (pageable.getPageNumber() + 1) * pageable.getPageSize() <= maxPages;
    }


    @Cacheable(value = "Seller:Products", key = "#sellerId + '_' + #status")
    public ProductWindow load(UUID sellerId, Product.ProductStatus status) {
        Pageable pageable = PageRequest.of(0, maxPages, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<Product> productsPage = productRepository.findAllBySellerId(sellerId, pageable, status);

        List<Product> productList = productsPage.getContent();
        List<UUID> productIdsList = new ArrayList<>();
        for(Product product: productList) productIdsList.add(product.getId());

        List<List<Integer>> inventoryInfo = inventoryClient.getTotalStockThatAdded(productIdsList);
        List<ProductResponse> productResponseList = new ArrayList<>();
        for(int i=0;i<productIdsList.size();i++) {
            productResponseList.add(ProductResponse.fromEntity(productList.get(i), inventoryInfo.get(i).get(0), inventoryInfo.get(i).get(1)));
        }

        return new ProductWindow(productResponseList, productResponseList.size());
    }
}
