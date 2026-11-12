package com.Order.repository;

import com.Order.entity.OrderItem;
import com.Order.entity.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {


    OrderItem findByOrder(Orders order);


    @Query("""
    SELECT oi
    FROM OrderItem oi
    WHERE (oi.status <> 'PENDING' AND (:status IS NULL OR oi.status = :status ))
      AND oi.sellerId = :id
    """)
    Page<OrderItem> findOrdersOfSeller(UUID id, OrderItem.OrderItemStatus status, Pageable pageable);


    @Query("""
        SELECT oi FROM OrderItem oi
        WHERE oi.createdAt BETWEEN :start AND :end
        AND oi.sellerId = :id
        """)
    List<OrderItem> getOrOrderItemsBetweenThisDuration(UUID id, Instant start, Instant end);
}
