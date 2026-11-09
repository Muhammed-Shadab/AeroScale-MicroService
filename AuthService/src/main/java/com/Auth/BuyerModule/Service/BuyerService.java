package com.Auth.BuyerModule.Service;


import com.Auth.BuyerModule.DTO.Request.AddAddressRequest;
import com.Auth.BuyerModule.DTO.Request.BuyerProfileUpdateRequest;
import com.Auth.BuyerModule.DTO.Response.AddAddressResponse;
import com.Auth.BuyerModule.DTO.Response.BuyerProfileResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface BuyerService {
    void updateProfile(BuyerProfileUpdateRequest buyerProfileUpdateRequest, UUID id);

    AddAddressResponse addAddress(AddAddressRequest addAddressRequest, UUID id);

//    this is for cross-module communication (Order -> Buyer)
    AddAddressResponse getBuyerAddressForCheckout(UUID buyerId, UUID addressId);

    List<AddAddressResponse> getAllAddress(UUID id);

    void deleteAddress(UUID id, UUID addId);

    void updateAddress(UUID id, UUID addId, @Valid AddAddressRequest newAddress);

    BuyerProfileResponse getBuyerProfile(UUID id, String email);
}
