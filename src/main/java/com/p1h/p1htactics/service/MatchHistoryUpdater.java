package com.p1h.p1htactics.service;

import com.p1h.p1htactics.entity.Summoner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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
    private final static int interval = 1;
    @Value("${riot.api.history.default}")
    private int defaultCount;

    /**
     * get all summoners
     * for each summoner query riotApi for match history (by puuid)
     * update match history with new ids + lastUpdated
     */
    @Scheduled(fixedRate = interval * 60 * 10)
    public void updateMatchHistory() {
        log.info("Scheduled match history updater has started...");

        userService.getAllSummoners()
                .forEach(summoner -> riotApiService.getMatchHistoryByPuuId(summoner.getPuuid(), defaultCount)
                        .flatMap(matchIds -> updateSummonerMatchHistory(matchIds, summoner)));

    }

    private Mono<Void> updateSummonerMatchHistory(List<String> newMatchIds, Summoner summoner) {
        var existingMatches = summoner.getMatchHistory();
        newMatchIds.removeAll(existingMatches);
        existingMatches.addAll(newMatchIds);

        summoner.setMatchHistory(existingMatches);
        return Mono.just(userService.saveSummoner(summoner)).then();
    }

}
