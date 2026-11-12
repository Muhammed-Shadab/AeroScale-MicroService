package com.product.repository;


import com.product.entity.Product;

import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;

public final class ProductSpecs {
    private ProductSpecs() {}

    public static Specification<Product> withStatus(Product.ProductStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"),
                (status == null) ? Product.ProductStatus.ACTIVE: status);
    }

    public static Specification<Product> textContains(String q) {
        return (root, query, cb) -> {
            String like = likePattern(q);
            return cb.or(
                    cb.like(cb.lower(root.<String>get("name")), like, '\\'),
                    cb.like(cb.lower(root.<String>get("description")), like, '\\'));
        };
    }

    public static Specification<Product> priceAtLeast(BigDecimal min) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.<BigDecimal>get("price"), min);
    }

    public static Specification<Product> priceAtMost(BigDecimal max) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.<BigDecimal>get("price"), max);
    }

//    // Inventory mei  totalStock - lockedStock > 0
//    public static Specification<Product> inStock() {
//        return (root, query, cb) -> {
//            Subquery<Integer> sub = query.subquery(Integer.class);
//            Root<Inventory> inv = sub.from(Inventory.class);
//            sub.select(cb.literal(1)).where(
//                    cb.equal(inv.get("productId"), root.get("id")),
//                    cb.greaterThan(cb.diff(inv.<Integer>get("totalStock"), inv.<Integer>get("lockedStock")), 0));
//            return cb.exists(sub);
//        };
//    }
//
//    /** Aisa koi FlashSale nahi jo SCHEDULED/ACTIVE ho aur abhi khatam na hua ho */
//    public static Specification<Product> regularOnly() {
//        return (root, query, cb) -> {
//            Subquery<Integer> sub = query.subquery(Integer.class);
//            Root<FlashSale> fs = sub.from(FlashSale.class);
//            sub.select(cb.literal(1)).where(
//                    cb.equal(fs.get("productId"), root.get("id")),
//                    fs.get("status").in(FlashSale.FlashSaleStatus.SCHEDULED, FlashSale.FlashSaleStatus.ACTIVE),
//                    cb.greaterThan(fs.<Instant>get("endTime"), Instant.now()));
//            return cb.not(cb.exists(sub));
//        };
//    }

    // user ke "%" ya "_" ko literal maanne ke liye escape
    private static String likePattern(String q) {
        String esc = q.trim().toLowerCase()
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + esc + "%";
    }
}