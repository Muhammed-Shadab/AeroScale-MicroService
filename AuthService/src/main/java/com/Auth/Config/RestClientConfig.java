package com.Auth.Config;

import com.Auth.RestClientTokenPropagator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient(RestClientTokenPropagator propagator) {
        return RestClient
                .builder()
                .requestInterceptor(propagator)
                .build();
    }
}
