package com.product.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;


    @Column(nullable = false)
    private Instant startTime;

    @Column(nullable = false)
    private Instant endTime;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxItemsPerUser = 4;

    // sale ke dauran buyer jo price dega
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salePrice;

    // is sale ke liye alag rakhi gayi units
    @Column(nullable = false)
    private Integer totalQuantity;

    @Column(nullable = false)
    @Builder.Default
    private Integer soldQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status = ProductStatus.SCHEDULED; // Default is DRAFT

    public enum ProductStatus {
        SCHEDULED, ACTIVE, ENDED, CANCELLED
    }

    @PrePersist
    protected void init() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void update() {
        this.updatedAt = Instant.now();
    }

}
