package com.Auth.BuyerModule.Service.Imp;

import com.Auth.BuyerModule.DTO.Request.AddAddressRequest;
import com.Auth.BuyerModule.DTO.Request.BuyerProfileUpdateRequest;
import com.Auth.BuyerModule.DTO.Request.CheckoutRequest;
import com.Auth.BuyerModule.DTO.Response.AddAddressResponse;
import com.Auth.BuyerModule.DTO.Response.BuyerProfileResponse;
import com.Auth.BuyerModule.DTO.Response.CartResponse;
import com.Auth.BuyerModule.Entity.Buyer;
import com.Auth.BuyerModule.Entity.BuyerAddress;
import com.Auth.BuyerModule.Service.CartService;
import com.Auth.Exceptions.BuyerException.EmptyCartException;
import com.Auth.Exceptions.BuyerException.RequiredThingsNotFoundException;
import com.Auth.BuyerModule.Repository.BuyerAddressRepository;
import com.Auth.BuyerModule.Repository.BuyerRepository;

import com.Auth.BuyerModule.Service.BuyerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BuyerServiceImp implements BuyerService {

    private final BuyerRepository buyerRepository;
    private final BuyerAddressRepository buyerAddressRepository;
    private final CartService cartService;

    @Override
    @Transactional
    public void updateProfile(BuyerProfileUpdateRequest buyerProfileUpdateRequest, @AuthenticationPrincipal(expression = "id") UUID id) {
        Buyer buyer  = findBuyerOrThrow(id);

        buyer.setFullName(buyerProfileUpdateRequest.getFullName());
        buyer.setDateOfBirth(buyerProfileUpdateRequest.getDob());

        buyerRepository.save(buyer);
    }

    @Override
    @Transactional
    public AddAddressResponse addAddress(AddAddressRequest addAddressRequest, UUID id) {
        Buyer buyer = findBuyerOrThrow(id);
        int count = buyerAddressRepository.countByBuyerId(id);

        if(count == 0) addAddressRequest.setDefault(true);
        else if(addAddressRequest.isDefault()) buyerAddressRepository.clearDefaultForBuyer(id);
        BuyerAddress buyerAddress = BuyerAddress.builder()
                .addressLabel(addAddressRequest.getLabel())
                .buyer(buyer)
                .addressLabel(addAddressRequest.getLabel())
                .addressLine1(addAddressRequest.getAddressLine1())
                .addressLine2(addAddressRequest.getAddressLine2())
                .city(addAddressRequest.getCity())
                .country(addAddressRequest.getCountry())
                .state(addAddressRequest.getState())
                .pincode(addAddressRequest.getPincode())
                .isDefault(addAddressRequest.isDefault())
                .recipientName(addAddressRequest.getRecipientName())
                .recipientPhoneNo(addAddressRequest.getPhoneNo())
                .build();


        BuyerAddress savedAddress = buyerAddressRepository.save(buyerAddress);
        return BuyerAddressToAddAddressResponseConverter(savedAddress);
    }

    @Override
    public AddAddressResponse getBuyerAddressForCheckout(UUID buyerId, UUID addressId) {
        BuyerAddress address = buyerAddressRepository.findById(addressId)
                .orElseThrow(() -> new RequiredThingsNotFoundException("Shipping address not found"));

        if (!address.getBuyer().getId().equals(buyerId)) {
            throw new RequiredThingsNotFoundException("Address does not belong to the current buyer");
        }

        return BuyerAddressToAddAddressResponseConverter(address);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddAddressResponse> getAllAddress(UUID id) {
        List<BuyerAddress> addressList = buyerAddressRepository.findAllByBuyerId(id);

        List<AddAddressResponse> responseList = new ArrayList<>();
        for(BuyerAddress address: addressList) {
            responseList.add(BuyerAddressToAddAddressResponseConverter(address));
        }
        return responseList;
    }

    @Override
    @Transactional
    public void deleteAddress(UUID id, UUID addId) {
        // default wala thing is not handle  it in the order service or here only
        BuyerAddress address = findAddressByBuyerOrThrow(addId, id);

        buyerAddressRepository.delete(address);
    }

    @Override
    @Transactional
    public void updateAddress(UUID id, UUID addId, AddAddressRequest newAddress) {
        BuyerAddress address = findAddressByBuyerOrThrow(addId, id);

        if(newAddress.isDefault() && !address.isDefault()) buyerAddressRepository.clearDefaultForBuyer(id);

        address.setAddressLabel(newAddress.getLabel());
        address.setRecipientName(newAddress.getRecipientName());
        address.setRecipientPhoneNo(newAddress.getPhoneNo());
        address.setAddressLine1(newAddress.getAddressLine1());
        address.setAddressLine2(newAddress.getAddressLine2());
        address.setCity(newAddress.getCity());
        address.setState(newAddress.getState());
        address.setCountry(newAddress.getCountry());
        address.setPincode(newAddress.getPincode());
        address.setDefault(newAddress.isDefault());

        buyerAddressRepository.save(address);
    }

    @Override
    public BuyerProfileResponse getBuyerProfile(UUID id, String email) {
        Buyer buyer  = findBuyerOrThrow(id);

        return BuyerProfileResponse.builder()
                .dob(buyer.getDateOfBirth())
                .email(email)
                .fullName(buyer.getFullName())
                .loyaltyPoint(buyer.getLoyaltyPoint())
                .walletBalance(buyer.getWalletBalance())
                .id(buyer.getId())
                .build();
    }
//
//    @Override
//    public String fetchCartAndAddressForCheckout(UUID buyerId, CheckoutRequest request) {
////        Optional<Orders> existingOrder = orderRepository.findByIdempotencyKey(request.idempotencyKey());
////        if (existingOrder.isPresent()) {
////            return OrderResponse.fromEntity(existingOrder.get(), paymentService.getRazorPayOrderId(existingOrder.get().getId()), key, null);
////        }
//
//        // 2. Fetch Cart
//        List<CartResponse> cartItems = cartService.getAllCarts(buyerId);
//        if (cartItems == null || cartItems.isEmpty()) {
//            throw new EmptyCartException("Cannot place an order with an empty cart");
//        }
//
//
//        AddAddressResponse address = getBuyerAddressForCheckout(buyerId, request.shippingAddressId());
//
//
//    }

    private BuyerAddress findAddressByBuyerOrThrow(UUID addId, UUID id) {
        return buyerAddressRepository.findByIdAndBuyerId(addId, id)
                .orElseThrow(() -> new RequiredThingsNotFoundException("Address Not Found!!"));
    }

    private AddAddressResponse BuyerAddressToAddAddressResponseConverter(BuyerAddress buyerAddress) {
        return AddAddressResponse.builder()
                .id(buyerAddress.getId())
                .label(buyerAddress.getAddressLabel())
                .addressLine1(buyerAddress.getAddressLine1())
                .addressLine2(buyerAddress.getAddressLine2())
                .city(buyerAddress.getCity())
                .state(buyerAddress.getState())
                .country(buyerAddress.getCountry())
                .pincode(buyerAddress.getPincode())
                .createdAt(buyerAddress.getCreatedAt())
                .updatedAt(buyerAddress.getUpdatedAt())
                .isDefault(buyerAddress.isDefault())
                .recipientName(buyerAddress.getRecipientName())
                .recipientPhoneNo(buyerAddress.getRecipientPhoneNo())
                .build();
    }

    private Buyer findBuyerOrThrow(UUID id) {
        return buyerRepository.findById(id)
                .orElseThrow(() -> new RequiredThingsNotFoundException("User Not Found"));
    }
}
