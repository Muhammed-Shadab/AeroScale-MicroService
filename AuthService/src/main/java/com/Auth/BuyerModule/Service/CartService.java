package com.Auth.BuyerModule.Service;

import com.Auth.BuyerModule.DTO.Request.ItemDataForCart;
import com.Auth.BuyerModule.DTO.Request.UpdateCartItemRequest;
import com.Auth.BuyerModule.DTO.Response.CartResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface CartService {

    void addItemToCart(ItemDataForCart itemDataForCart, UUID id);

    void removeItem(UUID itemId, UUID buyerId);

    List<CartResponse> getAllCarts(UUID id);

    void UpdateCartDetails(UpdateCartItemRequest updateCartItemRequest, UUID id);

    void clearCart(UUID id);
}
