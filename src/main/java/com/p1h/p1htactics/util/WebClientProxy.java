package com.p1h.p1htactics.util;

import lombok.AllArgsConstructor;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;


@AllArgsConstructor
public class WebClientProxy {

    private final WebClient webClient;

    public String get(String uri) {
       return webClient.get()
               .uri(uri)
               .retrieve()
               .bodyToMono(String.class)
               .block();
    }

    public int getStatusCode(String uri) {
        return webClient.get()
                .uri(uri)
                .exchangeToMono(ClientResponse::toBodilessEntity)
                .map(voidResponseEntity -> voidResponseEntity.getStatusCode().value())
                .block();
    }
}
