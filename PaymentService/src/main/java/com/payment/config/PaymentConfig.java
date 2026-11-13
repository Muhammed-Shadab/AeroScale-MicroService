package com.payment.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;




@Configuration
public class PaymentConfig {

    @Value("${razorpay-key}")
    private String key;

    @Value("${razorpay-secret-key}")
    private String secretKey;

    //Singleton RazorpayClient bean reused across all requests.
    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        return new RazorpayClient(key, secretKey);
    }

    //RestClient bean
    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }
}