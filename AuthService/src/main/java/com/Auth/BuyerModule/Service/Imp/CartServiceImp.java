package com.Auth.BuyerModule.Service.Imp;


import com.Auth.BuyerModule.DTO.Request.ItemDataForCart;
import com.Auth.BuyerModule.DTO.Request.ProductDetails;
import com.Auth.BuyerModule.DTO.Request.UpdateCartItemRequest;
import com.Auth.BuyerModule.DTO.Response.CartResponse;
import com.Auth.BuyerModule.Entity.Buyer;
import com.Auth.BuyerModule.Entity.CartItem;
import com.Auth.Exceptions.BuyerException.CartItemNotFoundException;
import com.Auth.Exceptions.BuyerException.RequiredThingsNotFoundException;
import com.Auth.BuyerModule.Repository.BuyerRepository;
import com.Auth.BuyerModule.Repository.CartRespository;
import com.Auth.BuyerModule.Service.CartService;
import com.Auth.Client.ProductClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartServiceImp implements CartService {

    private final CartRespository cartRespository;
    private final BuyerRepository buyerRepository;
    private final ProductClient productClient;


    @Override
    @Transactional
    public void addItemToCart(ItemDataForCart itemDataForCart, UUID id) {
        Buyer buyer = buyerRepository.findById(id).
                orElseThrow(() -> new RequiredThingsNotFoundException("Buyer Not Found!!"));

        boolean doesProductExist = productClient.doestThisProductExist(itemDataForCart.getProductId());

        CartItem existing = cartRespository.findByBuyerAndProduct(buyer, itemDataForCart.getProductId()).orElse(null);

        if(existing == null) {
            CartItem item = CartItem.builder()
                    .buyer(buyer)
                    .product(itemDataForCart.getProductId())
                    .itemCount(itemDataForCart.getCount())
                    .build();

            cartRespository.save(item);
        }else {
            existing.setItemCount(itemDataForCart.getCount());
            cartRespository.save(existing);
        }
    }

    @Override
    @Transactional
    public void removeItem(UUID itemId, UUID buyerId) {
        Buyer buyer = buyerRepository.findById(buyerId).
                orElseThrow(() -> new RequiredThingsNotFoundException("Buyer Not Found!!"));

        CartItem item = cartRespository.findByIdAndBuyerId(itemId, buyer.getId())
                .orElseThrow(() -> new CartItemNotFoundException("Product Not found in your cart!!"));
        cartRespository.delete(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartResponse> getAllCarts(UUID id) {
        List<CartItem> cartItemList = cartRespository.findAllByBuyerId(id);

        List<CartResponse> cartResponsesList = new ArrayList<>();
        for(CartItem item: cartItemList) {
            cartResponsesList.add(cartItemToCartResponseConverter(item));
        }
        return cartResponsesList;
    }

    @Override
    @Transactional
    public void UpdateCartDetails(UpdateCartItemRequest updateCartItemRequest, UUID buyerId) {
        Buyer buyer = buyerRepository.findById(buyerId).
                orElseThrow(() -> new RequiredThingsNotFoundException("Buyer NotFound!!"));

        CartItem item = cartRespository.findByIdAndBuyerId(updateCartItemRequest.getCartid(), buyer.getId())
                .orElseThrow(() -> new CartItemNotFoundException("Product Not Found in your Cart!!"));
//
//        Product product = item.getProduct();
//        assertStockAvailable(product, updateCartItemRequest.getItemCount());

        item.setItemCount(updateCartItemRequest.getItemCount());
        cartRespository.save(item);
    }

    @Override
    @Transactional
    public void clearCart(UUID id) {
        cartRespository.deleteAllByBuyerId(id);
    }

    private CartResponse cartItemToCartResponseConverter(CartItem item) {
        if(item == null) return null;
        UUID product =  item.getProduct();

        ProductDetails productDetails = productClient.getProductDetails(product);

        return CartResponse.builder()
                .id(item.getId())
                .productId(product)
                .itemCount(item.getItemCount())
                .itemName(productDetails.getName())
                .pricePerItem(productDetails.getPrice())
                .imageUrl(productDetails.getImageUrl())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private void assertStockAvailable(ProductDetails product, int reqQuantity) {

//        int stockCount = restClient.get()
//                .uri("http://localhost:8080/api/inv/getProductStock/" + product.getId())
//                .retrieve()
//                .body(Integer.class);
//
//
//        if(stockCount < reqQuantity) {
//            throw new InsufficientStockException("Only " + (stockCount)
//                    + " Units of " + product.getName() + " are available");
//        }
    }
}
