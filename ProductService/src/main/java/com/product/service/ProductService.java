package com.product.service;

import com.product.dto.request.ProductRequest;
import com.product.dto.request.RestockRequest;
import com.product.dto.response.ProductResponse;
import com.product.entity.Product;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ProductService {

    ProductResponse createProduct(UUID sellerId , ProductRequest request , String imageUrl);

    ProductResponse getProductByIdAndSellerId(UUID productId, UUID sellerId);

    Page<ProductResponse> getAllProductsBySellerId(UUID sellerId, Pageable pageable, Product.ProductStatus status, String q);

    ProductResponse updateProduct(UUID productId , UUID sellerId , ProductRequest request , String imageUrl);

    void deleteProduct(UUID productId ,  UUID sellerId);

    // Internal cross-module method for Order module
    ProductResponse getProductForCheckout(UUID productId);

    String getProductNameByProductId(UUID productId);

    String getProductImageByProductId(UUID productId);

    Product findProductByIdOrThrow(UUID productId);

    List<ProductResponse> getProductsByIds(Collection<UUID> productIds);

    List<ProductResponse> ProductsToProductResponse(List<Product> content);

    boolean DoesProductExist(UUID productId);

    void restockProduct(UUID productId,RestockRequest request, UUID sellerId);
}
