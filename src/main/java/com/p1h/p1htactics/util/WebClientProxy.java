package com.p1h.p1htactics.util;

import lombok.AllArgsConstructor;
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
}
