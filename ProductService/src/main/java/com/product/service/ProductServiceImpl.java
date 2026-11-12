package com.product.service;

import com.product.Client.InventoryClient;
import com.product.cache.ProductCache;
import com.product.dto.request.ProductRequest;
import com.product.dto.request.RestockRequest;
import com.product.dto.response.ProductBasicInfo;
import com.product.dto.response.ProductResponse;
import com.product.entity.Product;
import com.product.exception.ProductNotFoundException;
import com.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final InventoryClient inventoryClient;
    private final ProductCache productCache;

    @Override
    @Transactional
    public ProductResponse createProduct(UUID sellerId, ProductRequest request , String imageUrl) {
        Product product = Product.builder()
                .sellerId(sellerId)
                .name(request.name())
                .description((request.description()))
                .price(request.price())
                .imageUrl(imageUrl)
                .status(request.status())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .maxItemsPerUser(request.maxItemPerUser())
                .build();
        Product savedProduct = productRepository.save(product);
        inventoryClient.initializeInventory(savedProduct.getId());
        return ProductResponse.fromEntity(savedProduct, 0, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductByIdAndSellerId(UUID productId, UUID sellerId) {
        Product product = fetchProduct(productId , sellerId);
        List<List<Integer>> inventoryInfo = inventoryClient.getTotalStockThatAdded(List.of(productId));
        return ProductResponse.fromEntity(product, inventoryInfo.get(0).get(0), inventoryInfo.get(0).get(1));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProductsBySellerId(UUID sellerId, Pageable pageable,  Product.ProductStatus status, String q) {
        if(productCache.canCacheThisPage(pageable)) {
            return productCache.load(sellerId, status).slice(pageable);
        }

        Page<Product> productsPage = productRepository.findAllBySellerId(sellerId, pageable, status);
        List<ProductResponse> productResponseList = ProductsToProductResponse(productsPage.getContent());

        return new PageImpl<>(productResponseList, pageable, productResponseList.size());
    }


    @Override
    public List<ProductResponse> ProductsToProductResponse(List<Product> productList) {
        List<UUID> productIdsList = new ArrayList<>();
        for(Product product: productList) productIdsList.add(product.getId());

        List<List<Integer>> inventoryInfo = inventoryClient.getTotalStockThatAdded(productIdsList);
        List<ProductResponse> productResponseList = new ArrayList<>();
        for(int i=0;i<productIdsList.size();i++) {
            productResponseList.add(ProductResponse.fromEntity(productList.get(i), inventoryInfo.get(i).get(0), inventoryInfo.get(i).get(1)));
        }
        return productResponseList;
    }

    @Override
    public boolean DoesProductExist(UUID productId) {
        return productRepository.existsById(productId);
    }

    @Override
    public void restockProduct(UUID productId, RestockRequest request, UUID sellerId) {
        fetchProduct(productId, sellerId);
        inventoryClient.addStock(productId, request.quantity());
    }

    @Override
    public boolean DoesProductExistBySeller(UUID productId, UUID sellerId) {
        return productRepository.existsByIdAndSellerId(productId, sellerId);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(UUID productId, UUID sellerId, ProductRequest request , String imageUrl) {
        Product product = fetchProduct(productId, sellerId);

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStatus(request.status());
        product.setEndTime(request.endTime());
        product.setMaxItemsPerUser(request.maxItemPerUser());

        if (imageUrl != null) product.setImageUrl(imageUrl); // agar new img ayegi toh hi updte wnra no update

        Product updatedProduct = productRepository.save(product);
        List<List<Integer>> inventoryInfo = inventoryClient.getTotalStockThatAdded(List.of(productId));
        return ProductResponse.fromEntity(updatedProduct, inventoryInfo.get(0).get(0), inventoryInfo.get(0).get(1));
    }

    @Override
    @Transactional
    public void deleteProduct(UUID productId, UUID sellerId) {
        Product product = fetchProduct(productId, sellerId);

        // IMP : DO NOT DO HARD DELETE.WE DO Soft delete: Change status to ARCHIVED.
        if(product.getStatus().toString().equals("ACTIVE") || product.getStatus().toString().equals("SCHEDULED"))
            product.setStatus(Product.ProductStatus.CANCELLED);

        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductForCheckout(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        if (product.getStatus() != Product.ProductStatus.ACTIVE) {
            throw new IllegalStateException("Product is no longer available: " + product.getName());
        }

        return ProductResponse.fromEntity(product, 0, 0);
    }



    @Override
    public Map<UUID, ProductBasicInfo> getProductBasicInfo(List<UUID> productIds) {
       Map<UUID, ProductBasicInfo> response = new HashMap<>();

       for(UUID productId: productIds) {
           Product product = productRepository.findById(productId)
                   .orElseThrow(() -> new ProductNotFoundException("Product is not Available!!"));

           response.put(productId, ProductBasicInfo.builder()
                   .imageUrl(product.getImageUrl())
                   .productName(product.getName())
                   .productId(product.getId())
                   .unitPrice(product.getPrice())
                   .build());
       }
       return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByIds(
            Collection<UUID> productIds) {

        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }

        List<Product> productList = productRepository.findAllById(productIds);
        List<ProductResponse> res = new ArrayList<>();

        for(int i=0;i<productList.size();i++) {
            res.add(ProductResponse.fromEntity(productList.get(i), 0, 0));
        }
        return res;

    }


    // Helper method to enforce data isolation (DRY Principle)
    private Product fetchProduct(UUID productId, UUID sellerId) {
        return productRepository.findByIdAndSellerId(productId, sellerId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found or you do not have permission to access it"));
    }
}
