package com.p1h.p1htactics.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.p1h.p1htactics.dto.*;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
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
                .map(placement -> placementMapper(placement, gameMode))
                .limit(limit)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        return BigDecimal.valueOf(average)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public List<SummonerRankingDto> getRankings(String selectedSet, String selectedMode) {
        var summonersToGetRankingFor = getCurrentUserAndFriendsMapped(SummonerMapper::summonerToSummonerDto);
        return summonersToGetRankingFor.stream()
                .map(summonerDto -> SummonerMapper.summonerDtoToSummonerRankingDto(
                        summonerDto,
                        getAvgPlacementBySet(summonerDto.gameName(), summonerDto.tag(), selectedMode, selectedSet, 1000)))
                .toList();
    }

    public List<DuoResults> getBestDuo(String set) {
        var summonersToGetRankingFor = getCurrentUserAndFriendsMapped(Summoner::getGameName);
        var doubleUpMatches = matchRepository.findBySummonerNameInAndGameModeAndSet(summonersToGetRankingFor, "1160", set);

        Map<Duo, List<Integer>> duoMap = new HashMap<>();

        var matchesGroupedByMatchId = doubleUpMatches.stream()
                .collect(Collectors.groupingBy(Match::getMatchId));

        for (var match : matchesGroupedByMatchId.values()) {
            var m1 = match.get(0);
            var m2 = match.get(1);

            var namesSorted = Stream.of(m1.getSummonerName(), m2.getSummonerName()).sorted().toList();
            var duo = new Duo(namesSorted.get(0), namesSorted.get(1));
            var duoPlacement = getDoubleUpPlacement(m1.getPlacement());

            duoMap.computeIfAbsent(duo, k -> new ArrayList<>()).add(duoPlacement);
        }

        var duoResultsList = duoMap.entrySet().stream()
                .map(entry -> {
                    var duo = entry.getKey();
                    var placements = entry.getValue();
                    var avg = entry.getValue().stream()
                            .mapToInt(Integer::intValue)
                            .average()
                            .orElse(0.0);
                    avg = BigDecimal.valueOf(avg)
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();
                    return new DuoResults(duo, placements, avg);
                })
                .toList();

        return duoResultsList;
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

    public Map<String, SummonerRankingStats> getRankingStatsBy(String gameName, String tag) {
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

    public List<MatchHistoryDto> getMatchHistory(String summonerName) {
        var matchesList = matchRepository.findTop10BySummonerNameOrderByGameTimeDesc(summonerName);
        return matchesList.stream()
                .map(match -> new MatchHistoryDto(
                        SummonerMapper.gameModeMapper(match.getGameMode()),
                        match.getPlacement(),
                        match.getTraits(),
                        match.getGameTime().toLocalDate()))
                .toList();
    }

    private String getSummonerId(String accountInfo) {
        try {
            return objectMapper.readTree(accountInfo).get("id").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error while processing summoner ID JSON.");
        }
    }

    private Map<String, SummonerRankingStats> getRankedStats(String accountStats) {
        try {
            if (accountStats.equals("[]")) {
                return Map.of("RANKED_TFT", new SummonerRankingStats("UNKNOWN", "UNKNOWN", 0),
                        "RANKED_TFT_DOUBLE_UP", new SummonerRankingStats("UNKNOWN", "UNKNOWN", 0));
            }

            var stats = objectMapper.readValue(
                    accountStats,
                    new TypeReference<List<RankedStatsDto>>() {}
            );

            return stats.stream()
                    .filter(s -> "RANKED_TFT".equals(s.queueType()) || "RANKED_TFT_DOUBLE_UP".equals(s.queueType()))
                    .collect(Collectors.toMap(
                            RankedStatsDto::queueType,
                            SummonerMapper::RankedStatsDtoToSummonerRankingStats
                    ));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error while processing ranked rankedStats JSON.");
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

    private Integer placementMapper(int placement, String gameMode) {
        if (gameMode.equals("1160")) {
            return getDoubleUpPlacement(placement);
        }
        return placement;
    }

    private int getDoubleUpPlacement(int placement) {
        return switch (placement) {
            case 1, 2 -> 1;
            case 3, 4 -> 2;
            case 5, 6 -> 3;
            default   -> 4;
        };
    }

    private <R> List<R> getCurrentUserAndFriendsMapped(Function<Summoner, R> summonerMapper) {
        var currentLoggedSummoner = userRepository.findByUsername(UserUtils.getCurrentUsername()).orElseThrow();

        var friends = Optional.ofNullable(currentLoggedSummoner.getFriends())
                .orElse(List.of())
                .stream()
                .map(friendDto -> userRepository.findSummonerByGameNameAndTag(friendDto.gameName(), friendDto.tag()))
                .flatMap(Optional::stream)
                .toList();

        return Stream.concat(Stream.of(currentLoggedSummoner), friends.stream())
                .map(summonerMapper)
                .toList();
    }
}
