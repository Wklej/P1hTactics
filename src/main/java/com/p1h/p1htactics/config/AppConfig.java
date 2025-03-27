package com.p1h.p1htactics.config;

import com.p1h.p1htactics.util.WebClientProxy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private final ApiKeyProvider apiKeyProvider;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.set("X-Riot-Token", apiKeyProvider.getApiKey());
                    httpHeaders.set("Content-Type", "application/json");
                })
                .build();
    }

    @Bean
    public WebClientProxy webClientProxy() {
        return new WebClientProxy(webClient());
    }
}
