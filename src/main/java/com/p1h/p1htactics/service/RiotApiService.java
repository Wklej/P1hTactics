package com.p1h.p1htactics.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.p1h.p1htactics.util.WebClientProxy;
import lombok.AllArgsConstructor;
import org.springframework.data.web.JsonPath;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@AllArgsConstructor
public class RiotApiService {

    private final WebClientProxy webClientProxy;
    private final ObjectMapper objectMapper;

    public Mono<String> getAccountByRiotId(String gameName, String tagLine) {
        var accountInfoUri = String.format("/riot/account/v1/accounts/by-riot-id/%s/%s", gameName, tagLine);
        return webClientProxy.get(accountInfoUri);
    }

    public Mono<List<String>> getMatchHistoryByPuuId(String puuid, int count) {
        var matchHistory = String.format("/tft/match/v1/matches/by-puuid/%s/ids?count=%s", puuid, count);
        return webClientProxy.get(matchHistory)
                .flatMap(jsonResponse -> {
                    try {
                        return Mono.just(objectMapper.readValue(jsonResponse, new TypeReference<>() {}));
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException("Error while parsing match history: ", e));
                    }
                });
    }

    public Mono<String> getPuuId(String gameName, String tag) {
        return getAccountByRiotId(gameName, tag)
                .flatMap(jsonResponse -> {
                    try {
                        var jsonNode = objectMapper.readTree(jsonResponse);
                        var puuId = jsonNode.get("puuid").asText();
                        return Mono.just(puuId);
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException("Error while processing JSON: ", e));
                    }
                });
    }
}
