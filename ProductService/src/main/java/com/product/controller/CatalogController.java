package com.product.controller;

import com.product.dto.request.CatalogQuery;
import com.product.dto.response.CatalogResponse;
import com.product.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/catalog/products")
@RequiredArgsConstructor
public class CatalogController {

    private static final Set<String> SORTABLE = Set.of("createdAt", "price", "name");

    private final CatalogService catalogService;

    @GetMapping
    public ResponseEntity<Page<CatalogResponse>> getActiveProducts(
            @ModelAttribute CatalogQuery query,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        pageable.getSort().forEach(o -> {
            if (!SORTABLE.contains(o.getProperty()))
                throw new IllegalArgumentException("Cannot sort by '" + o.getProperty() + "'");
        });

        return ResponseEntity.ok(catalogService.searchProducts(
                query, pageable));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<CatalogResponse> getProductById(@PathVariable UUID productId) {
        return ResponseEntity.ok(catalogService.getActiveProductById(productId));
    }
}