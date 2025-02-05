package com.p1h.p1htactics.service;

import com.p1h.p1htactics.entity.Summoner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchHistoryUpdater {

    private final RiotApiService riotApiService;
    private final UserService userService;
    /**
        Interval in minutes.
     */
    private final static int interval = 30;
    @Value("${riot.api.history.default}")
    private int defaultCount;


    @Scheduled(fixedRate = interval * 60 * 1000)
    public void updateMatchHistory() {
        log.info("Scheduled match history updater has started...");

        userService.getAllSummoners()
                .forEach(summoner -> {
                    var newMatchHistory = riotApiService.getMatchHistoryByPuuId(summoner.getPuuid(), defaultCount);
                    updateSummonerMatchHistory(newMatchHistory, summoner);
                });

    }

    private void updateSummonerMatchHistory(List<String> newMatchIds, Summoner summoner) {
        var existingMatches = summoner.getMatchHistory();
        newMatchIds.removeAll(existingMatches);

        if (!newMatchIds.isEmpty()) {
            existingMatches.addAll(newMatchIds);
            summoner.setMatchHistory(existingMatches);
            userService.saveSummoner(summoner);
            log.info("Summoner " + summoner.getGameName() + " has been updated.");
        }
    }

}
