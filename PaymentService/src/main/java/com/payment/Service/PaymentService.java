package com.payment.Service;

import com.payment.DTO.Request.CreatePaymentOrderRequest;
import com.payment.DTO.Request.PaymentStatus;
import com.payment.DTO.Response.RazorPayOrderResponse;
import com.razorpay.RazorpayException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface PaymentService{
    RazorPayOrderResponse createRazorPayOrder(CreatePaymentOrderRequest request) throws RazorpayException;

    String getRazorPayOrderId(UUID orderId);

    void updatePaymentStatus(PaymentStatus paymentUpdate, UUID id) throws Exception;
}
