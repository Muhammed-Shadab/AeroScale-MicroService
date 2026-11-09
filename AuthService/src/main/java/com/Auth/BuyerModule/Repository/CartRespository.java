package com.Auth.BuyerModule.Repository;


import com.Auth.BuyerModule.Entity.Buyer;
import com.Auth.BuyerModule.Entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartRespository extends JpaRepository<CartItem, UUID> {

    Optional<CartItem> findByBuyerAndProduct(Buyer buyer, UUID product);

    Optional<CartItem> findByIdAndBuyerId(UUID id, UUID buyerid);

    List<CartItem> findAllByBuyerId(UUID buyerid);

    void deleteAllByBuyerId(UUID buyerid);
}
