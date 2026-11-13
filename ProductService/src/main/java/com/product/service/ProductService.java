package com.product.service;

import com.product.dto.request.ProductRequest;
import com.product.dto.request.RestockRequest;
import com.product.dto.response.ProductBasicInfo;
import com.product.dto.response.ProductBatchInfo;
import com.product.dto.response.ProductResponse;
import com.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ProductService {

    ProductResponse createProduct(UUID sellerId , ProductRequest request , String imageUrl);

    ProductResponse getProductByIdAndSellerId(UUID productId, UUID sellerId);

    Page<ProductResponse> getAllProductsBySellerId(UUID sellerId, Pageable pageable, Product.ProductStatus status, String q);

    ProductResponse updateProduct(UUID productId , UUID sellerId , ProductRequest request , String imageUrl);

    void deleteProduct(UUID productId ,  UUID sellerId);

    // Internal cross-module method for Order module
    ProductResponse getProductForCheckout(UUID productId);


    Map<UUID, ProductBasicInfo> getProductBasicInfo(List<UUID> productId);

    List<ProductResponse> getProductsByIds(Collection<UUID> productIds);

    List<ProductResponse> ProductsToProductResponse(List<Product> content);

    boolean DoesProductExist(UUID productId);

    void restockProduct(UUID productId,RestockRequest request, UUID sellerId);

    boolean DoesProductExistBySeller(UUID productId, UUID sellerId);

    /**
     * Batch fetch seller ID and sale price for given product IDs.
     */
    Map<UUID, ProductBatchInfo> getProductBatchInfo(List<UUID> productIds);
}
