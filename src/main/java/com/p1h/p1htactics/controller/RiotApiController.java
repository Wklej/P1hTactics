package com.p1h.p1htactics.controller;

import com.p1h.p1htactics.service.RiotApiService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping
@AllArgsConstructor
public class RiotApiController {

    private final RiotApiService riotApiService;

    @GetMapping("/account/{gameName}/{tagLine}")
    public Mono<String> getAccount(@PathVariable String gameName, @PathVariable String tagLine) {
        return riotApiService.getAccountByRiotId(gameName, tagLine);
    }

    @GetMapping("/history/{puuid}")
    public Mono<List<String>> getMatchHistory(@PathVariable String puuid, @RequestParam(defaultValue = "1000") int count) {
        return riotApiService.getMatchHistoryByPuuId(puuid, count);
    }
}
