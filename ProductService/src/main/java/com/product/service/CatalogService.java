package com.product.service;

import com.product.dto.request.CatalogQuery;
import com.product.dto.response.CatalogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CatalogService {

    Page<CatalogResponse> searchProducts(CatalogQuery query, Pageable pageable);

    CatalogResponse getActiveProductById(UUID productId);

    //Page<CatalogResponse> getFlashSaleCatalog(SaleState state, int page, int size);
}