package com.p1h.p1htactics.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.p1h.p1htactics.dto.SummonerRankingDto;
import com.p1h.p1htactics.dto.SummonerRankingStats;
import com.p1h.p1htactics.entity.Match;
import com.p1h.p1htactics.entity.Summoner;
import com.p1h.p1htactics.mapper.SummonerMapper;
import com.p1h.p1htactics.repository.MatchRepository;
import com.p1h.p1htactics.repository.UserRepository;
import com.p1h.p1htactics.util.UserUtils;
import com.p1h.p1htactics.util.WebClientProxy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class RiotApiService {

    private final WebClientProxy webClientProxy;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    @Value("${riot.api.base-url}")
    private String baseUrl;
    @Value("${riot.api.base-url-eune}")
    private String baseUrlEune;

    public String getAccountByRiotId(String gameName, String tagLine) {
        var accountInfoUri = String.format("%s/riot/account/v1/accounts/by-riot-id/%s/%s", baseUrl, gameName, tagLine);
        return webClientProxy.get(accountInfoUri);
    }

    public int getAccountByRiotIdStatusCode(String gameName, String tagLine) {
        var accountInfoUri = String.format("%s/riot/account/v1/accounts/by-riot-id/%s/%s", baseUrl, gameName, tagLine);
        return webClientProxy.getStatusCode(accountInfoUri);
    }

    public List<String> getMatchHistoryByPuuId(String puuid, int count) {
        var matchHistory = String.format("%s/tft/match/v1/matches/by-puuid/%s/ids?count=%s", baseUrl, puuid, count);
        var response = webClientProxy.get(matchHistory);
        try {
            return objectMapper.readValue(response, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error while processing match history: ", e);
        }

    }

    public String getPuuId(String gameName, String tag) {
        var accountJson = getAccountByRiotId(gameName, tag);
        try {
            var jsonNode = objectMapper.readTree(accountJson);
            return jsonNode.get("puuid").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error while processing JSON: ", e);
        }
    }

    public double getAvgPlacementBySet(String gameName, String tagLine, String gameMode, String set, int limit) {
        var summoner = userRepository.findSummonerByGameNameAndTag(gameName, tagLine).orElseThrow();
        var matchHistory = summoner.getMatchHistory();
        Collections.reverse(matchHistory);

        var average = matchHistory.stream()
                .map(matchId -> getPlacementBy(matchId, summoner.getGameName(), gameMode, set))
                .flatMap(Optional::stream)
                .limit(limit)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        return BigDecimal.valueOf(average)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public List<SummonerRankingDto> getRankings(String selectedSet) {
        var currentLoggedSummoner = userRepository.findByUsername(UserUtils.getCurrentUsername()).orElseThrow();
        var friends = Optional.ofNullable(currentLoggedSummoner.getFriends())
                .orElse(List.of())
                .stream()
                .map(friendDto -> userRepository.findSummonerByGameNameAndTag(friendDto.gameName(), friendDto.tag()))
                .flatMap(Optional::stream)
                .toList();
        var summonersToGetRankingFor = Stream.concat(Stream.of(currentLoggedSummoner), friends.stream())
                .map(SummonerMapper::summonerToSummonerDto)
                .toList();

        return summonersToGetRankingFor.stream()
                .map(summonerDto -> SummonerMapper.summonerDtoToSummonerRankingDto(
                        summonerDto,
                        getAvgPlacementBySet(summonerDto.gameName(), summonerDto.tag(), "1100", selectedSet, 1000)))
                .toList();
    }

    public Optional<Integer> getPlacement(String gameMode, Summoner summoner, String details) throws JsonProcessingException {
        var jsonInfoNode = objectMapper.readTree(details).get("info");
        var gameModeNode = jsonInfoNode.get("queueId");
        if (gameModeNode != null && gameModeNode.asText().equals(gameMode)) {
            var participantsNode = jsonInfoNode.get("participants");
            return findParticipantByPuuId(participantsNode, summoner.getPuuid())
                    .map(p -> p.get("placement").asInt());
        } else {
            return Optional.empty();
        }
    }

    public SummonerRankingStats getRankingStatsBy(String gameName, String tag) {
        var puuid = getPuuId(gameName, tag);
        //send request to TFT-SUMMONER-V1 by-puuid/encPUUID and get id (summonerId)
        var accountIdRequest = String.format("%s/tft/summoner/v1/summoners/by-puuid/%s", baseUrlEune, puuid);
        var accountIdResponse = webClientProxy.get(accountIdRequest);
        var summonerId = getSummonerId(accountIdResponse);
        //then request to TFT-LEAGUE-V1 by-summoner/summonerId and filter response
        var accountStatsRequest = String.format("%s/tft/league/v1/entries/by-summoner/%s", baseUrlEune, summonerId);
        var accountStatsResponse = webClientProxy.get(accountStatsRequest);
        return getRankedStats(accountStatsResponse);
    }

    private String getSummonerId(String accountInfo) {
        try {
            return objectMapper.readTree(accountInfo).get("id").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error while processing summoner ID JSON.");
        }
    }

    private SummonerRankingStats getRankedStats(String accountStats) {
        // 0 - ranked, 1 - double up
        try {
            var rankedJson = objectMapper.readTree(accountStats).get(0);
            var tier = rankedJson.get("tier").asText();
            var rank = rankedJson.get("rank").asText();
            var points = rankedJson.get("leaguePoints").asInt();
            var summonerRank = String.format("%s %s", tier, rank);
            return new SummonerRankingStats(summonerRank, points);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error while processing ranked stats JSON.");
        }
    }

    private Optional<JsonNode> findParticipantByPuuId(JsonNode participantsNode, String puuId) {
        return StreamSupport.stream(participantsNode.spliterator(), false)
                .filter(p -> p.has("puuid") && p.get("puuid").asText().equals(puuId))
                .findFirst();
    }

    private Optional<Integer> getPlacementBy(String matchId, String summonerName, String gameMode, String set) {
        return matchRepository.findByMatchIdAndSummonerNameAndGameModeAndSet(matchId, summonerName, gameMode, set)
                .map(Match::getPlacement);
    }
}
