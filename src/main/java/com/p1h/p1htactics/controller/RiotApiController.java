package com.p1h.p1htactics.controller;

import com.p1h.p1htactics.dto.SummonerRankingDto;
import com.p1h.p1htactics.service.RiotApiService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@AllArgsConstructor
public class RiotApiController {

    private final RiotApiService riotApiService;

    @GetMapping("/account/{gameName}/{tagLine}")
    public String getAccount(@PathVariable String gameName, @PathVariable String tagLine) {
        return riotApiService.getAccountByRiotId(gameName, tagLine);
    }

    @GetMapping("/history/{puuid}")
    public List<String> getMatchHistory(@PathVariable String puuid, @RequestParam(defaultValue = "1000") int count) {
        return riotApiService.getMatchHistoryByPuuId(puuid, count);
    }

    @GetMapping("/history/avg/{gameName}/{tagLine}/{gameMode}")
    public double getAvgPlacement(@PathVariable String gameName,
                                  @PathVariable String tagLine,
                                  @PathVariable String gameMode,
                                  @RequestParam(defaultValue = "1000") int limit) {
        return riotApiService.getAvgPlacement(gameName, tagLine, gameMode, limit);
    }

    @GetMapping("/api/getRanking")
    public List<SummonerRankingDto> getRanking() {
        return riotApiService.getRankings();
    }
}
