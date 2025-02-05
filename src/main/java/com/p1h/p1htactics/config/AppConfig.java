package com.p1h.p1htactics.config;

import com.p1h.p1htactics.util.WebClientProxy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    @Value("${riot.api.key}")
    private String apiKey;

    @Value("${riot.api.base-url}")
    private String baseUrl;


    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.set("X-Riot-Token", apiKey);
                    httpHeaders.set("Content-Type", "application/json");
                })
                .build();
    }

    @Bean
    public WebClientProxy webClientProxy() {
        return new WebClientProxy(webClient());
    }
}
