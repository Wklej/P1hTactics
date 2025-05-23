package com.p1h.p1htactics.controller;

import com.p1h.p1htactics.dto.DuoResults;
import com.p1h.p1htactics.dto.MatchHistoryDto;
import com.p1h.p1htactics.dto.SummonerRankingDto;
import com.p1h.p1htactics.service.RiotApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
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

    @GetMapping("/history/avg/{gameName}/{tagLine}/{gameMode}/{set}")
    public double getAvgPlacement(@PathVariable String gameName,
                                  @PathVariable String tagLine,
                                  @PathVariable String gameMode,
                                  @PathVariable String set,
                                  @RequestParam(defaultValue = "1000") int limit) {
        return riotApiService.getAvgPlacementBySet(gameName, tagLine, gameMode, set, limit);
    }

    @GetMapping("/api/getRanking/{selectedSet}/{selectedMode}")
    public List<SummonerRankingDto> getRanking(@PathVariable String selectedSet, @PathVariable String selectedMode) {
        return riotApiService.getRankings(selectedSet, selectedMode);
    }

    @GetMapping("api/ranking/bestDuo/{set}")
    public List<DuoResults> getBestDuoRanking(@PathVariable String set) {
        return riotApiService.getBestDuo(set);
    }

    @GetMapping("/api/matchHistory/{summonerName}")
    public List<MatchHistoryDto> getMatchHistory(@PathVariable String summonerName) {
        return riotApiService.getMatchHistory(summonerName);
    }

}
