package com.Auth.BuyerModule.Controller;


import com.Auth.BuyerModule.DTO.Request.AddAddressRequest;
import com.Auth.BuyerModule.DTO.Request.BuyerProfileUpdateRequest;
import com.Auth.BuyerModule.DTO.Request.CheckoutRequest;
import com.Auth.BuyerModule.DTO.Response.AddAddressResponse;
import com.Auth.BuyerModule.DTO.Response.BuyerProfileResponse;
import com.Auth.BuyerModule.Service.BuyerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/buyer")
@RequiredArgsConstructor
public class BuyerController {

    private final BuyerService buyerService;

    @PostMapping("/updateProfile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody BuyerProfileUpdateRequest buyerProfileUpdateRequest,
                                           @AuthenticationPrincipal(expression = "id") UUID id) {

            buyerService.updateProfile(buyerProfileUpdateRequest, id);
            return ResponseEntity.status(HttpStatus.CREATED.value()).build();
    }

    @PostMapping("/addAddress")
    public ResponseEntity<AddAddressResponse> addAddress(@Valid @RequestBody AddAddressRequest addAddressRequest,
                                                         @AuthenticationPrincipal(expression = "id") UUID id) {
        buyerService.addAddress(addAddressRequest, id);
        return ResponseEntity.status(HttpStatus.CREATED.value()).build();
    }

    @GetMapping("/getAllAddress")
    public ResponseEntity<List<AddAddressResponse>> getAllAddress(@AuthenticationPrincipal(expression = "id") UUID id) {
        return ResponseEntity.status(HttpStatus.CREATED.value()).body(buyerService.getAllAddress(id));
    }

    @DeleteMapping("/deleteAddress/{addId}")
    public ResponseEntity<?> deleteAddress(@AuthenticationPrincipal(expression = "id") UUID id
                                         , @PathVariable UUID addId) {
        buyerService.deleteAddress(id, addId);
        return ResponseEntity.status(HttpStatus.CREATED.value()).build();
    }

    @PutMapping("/updateAddress/{addId}")
    public ResponseEntity<?> updateAddress(@AuthenticationPrincipal(expression = "id") UUID id
                                            , @PathVariable UUID addId
                                            , @Valid @RequestBody AddAddressRequest newAddress) {
        buyerService.updateAddress(id, addId, newAddress);
        return ResponseEntity.status(HttpStatus.CREATED.value()).build();
    }

    @GetMapping("/profile")
    public ResponseEntity<BuyerProfileResponse> getBuyerProfile(@AuthenticationPrincipal(expression = "id") UUID id,
                                                                @AuthenticationPrincipal(expression = "email") String email) {
        return ResponseEntity.ok(buyerService.getBuyerProfile(id, email));
    }


    @GetMapping("/getAddressInfo/{id}")
    public ResponseEntity<AddAddressResponse> getBuyerAddressForCheckout(@PathVariable UUID id,
                                                         @AuthenticationPrincipal(expression = "id") UUID buyerId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(buyerService.getBuyerAddressForCheckout(buyerId, id));
    }


//    @PostMapping("/checkout")
//    @PreAuthorize("hasRole('BUYER')")
//    public ResponseEntity<String> checkout(
//            @Valid @RequestBody CheckoutRequest request,
//            @AuthenticationPrincipal(expression = "id") UUID buyerId){
//
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(buyerService.fetchCartAndAddressForCheckout(buyerId, request));
//    }


}
