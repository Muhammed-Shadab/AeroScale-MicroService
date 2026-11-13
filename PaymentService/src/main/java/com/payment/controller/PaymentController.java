package com.payment.controller;

import com.payment.DTO.Request.CreatePaymentOrderRequest;
import com.payment.DTO.Request.PaymentStatus;
import com.payment.DTO.Response.RazorPayOrderResponse;
import com.payment.Service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;


    @PostMapping("/create-order")
    public ResponseEntity<RazorPayOrderResponse> createOrder(@Valid @RequestBody CreatePaymentOrderRequest request) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createRazorPayOrder(request));
    }


    @PostMapping("/updatePaymentStatus")
    public ResponseEntity<Void> updatePaymentStatus(
            @Valid @RequestBody PaymentStatus paymentUpdate,
            @AuthenticationPrincipal(expression = "id") UUID buyerId) throws Exception {
        paymentService.updatePaymentStatus(paymentUpdate, buyerId);
        return ResponseEntity.ok().build();
    }
}