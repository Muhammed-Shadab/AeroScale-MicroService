package com.payment.Service;

import com.payment.DTO.Request.CreatePaymentOrderRequest;
import com.payment.DTO.Request.PaymentStatus;
import com.payment.DTO.Response.RazorPayOrderResponse;
import com.payment.Entity.Transactions;
import com.payment.Exception.RazorPayOrderIdNotFound;
import com.payment.client.OrderServiceClient;
import com.payment.repository.TransactionsRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImp implements PaymentService {

    @Value("${razorpay-key}")
    private String key;

    @Value("${razorpay-secret-key}")
    private String secretKey;

    private final TransactionsRepository transactionsRepository;
    private final RazorpayClient razorpayClient;
    private final OrderServiceClient orderServiceClient;



    @Override
    public RazorPayOrderResponse createRazorPayOrder(CreatePaymentOrderRequest request) throws RazorpayException {


        // rupees -> paise
        int amount = request.getAmount()
                .setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2)
                .intValueExact();

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "AeroScale_" + request.getOrderId());
        JSONObject notes = new JSONObject();
        notes.put("note1", "AeroScale");
        orderRequest.put("notes", notes);

        Order order = razorpayClient.orders.create(orderRequest);
        String rzpOrderId = order.get("id");

        Transactions transactions = Transactions.builder()
                .orderId(request.getOrderId())
                .buyerId(request.getBuyerId())
                .razorpayOrderId(rzpOrderId)
                .amount(request.getAmount())
                .status(Transactions.Status.PENDING)
                .build();
        transactionsRepository.save(transactions);

        return new RazorPayOrderResponse(key, amount, rzpOrderId);
    }

    @Override
    @Transactional(readOnly = true)
    public String getRazorPayOrderId(UUID orderId) {
        return transactionsRepository.findByOrderId(orderId)
                .map(Transactions::getRazorpayOrderId)
                .orElseThrow(() -> new RazorPayOrderIdNotFound("RazorPay Order Id not found"));
    }

    @Override
    @Transactional
    public void updatePaymentStatus(PaymentStatus payload, UUID buyerId) throws Exception {
        Transactions tx = transactionsRepository.findByRazorpayOrderId(payload.getRzpOrderId())
                .orElseThrow(() -> new RazorPayOrderIdNotFound("RazorPay Order Id not found"));


        if (!tx.getBuyerId().equals(buyerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Order does not belong to buyer");
        }

        //Idempotency....Return immediately if already completed
        if (tx.getStatus() == Transactions.Status.COMPLETED) {
            return;
        }

        // 3. Cryptographic HMAC-SHA256 signature verification
        if (payload.getPaymentId() == null || payload.getSignature() == null
                || !verifySignature(payload.getRzpOrderId(), payload.getPaymentId(), payload.getSignature())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment verification failed");
        }

        // 4. Update transaction status
        tx.setStatus(Transactions.Status.COMPLETED);
        tx.setPaymentId(payload.getPaymentId());
        tx.setSignature(payload.getSignature());
        transactionsRepository.save(tx);

        // 5. Single HTTP call to OrderService to confirm order and trigger inventory deduction
        orderServiceClient.confirmOrderPayment(tx.getOrderId(), buyerId);
    }

    private boolean verifySignature(String razorpayOrderId, String razorpayPaymentId,
                                    String razorpaySignature) throws Exception {
        String data = razorpayOrderId + "|" + razorpayPaymentId;

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // Razorpay signature hex hoti hai (Base64 nahi)
        String generated = HexFormat.of().formatHex(hash);

        return MessageDigest.isEqual(
                generated.getBytes(StandardCharsets.UTF_8),
                razorpaySignature.getBytes(StandardCharsets.UTF_8));
    }
}