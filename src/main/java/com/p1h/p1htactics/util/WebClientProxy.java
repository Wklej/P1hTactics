package com.p1h.p1htactics.util;

import lombok.AllArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@AllArgsConstructor
public class WebClientProxy {

    private final WebClient webClient;

    public Mono<String> get(String uri) {
       return webClient.get()
                .uri(uri)
                .retrieve()
//               .toString()
                .bodyToMono(String.class);
    }
}
