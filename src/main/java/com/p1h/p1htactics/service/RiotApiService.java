package com.p1h.p1htactics.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.p1h.p1htactics.dto.SummonerRankingDto;
import com.p1h.p1htactics.entity.Summoner;
import com.p1h.p1htactics.mapper.SummonerMapper;
import com.p1h.p1htactics.repository.MatchRepository;
import com.p1h.p1htactics.repository.UserRepository;
import com.p1h.p1htactics.util.UserUtils;
import com.p1h.p1htactics.util.WebClientProxy;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@AllArgsConstructor
public class RiotApiService {

    private final WebClientProxy webClientProxy;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;

    public String getAccountByRiotId(String gameName, String tagLine) {
        var accountInfoUri = String.format("/riot/account/v1/accounts/by-riot-id/%s/%s", gameName, tagLine);
        return webClientProxy.get(accountInfoUri);
    }

    public List<String> getMatchHistoryByPuuId(String puuid, int count) {
        var matchHistory = String.format("/tft/match/v1/matches/by-puuid/%s/ids?count=%s", puuid, count);
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

    public double getAvgPlacement(String gameName, String tagLine, String gameMode, int limit) {
        var summoner = userRepository.findSummonerByGameNameAndTag(gameName, tagLine).orElseThrow();
        var matchHistory = summoner.getMatchHistory();
        Collections.reverse(matchHistory);

        var average = matchHistory.stream()
                .map(matchId -> getPlacementIfMatchModeWithMatchId(matchId, gameMode, summoner))
                .flatMap(Optional::stream)
                .limit(limit)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        return BigDecimal.valueOf(average)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public double getAvgPlacementBySet(String gameName, String tagLine, String gameMode, String set, int limit) {
        var summoner = userRepository.findSummonerByGameNameAndTag(gameName, tagLine).orElseThrow();
        var matchHistory = summoner.getMatchHistory();
        Collections.reverse(matchHistory);

        var average = matchHistory.stream()
                .map(matchId -> getPlacementIfMatchModeAndSetWithMatchId(matchId, gameMode, summoner, set))
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

    private String getMatchDetails(String matchId) {
        return matchRepository.findFirstByMatchId(matchId).getDetails();
    }

    private Optional<String> getMatchDetailsIfSet(String matchId, String set) {
        var match = matchRepository.findFirstByMatchIdAndSet(matchId, set);
        return match.flatMap(value -> value.getDetails().describeConstable());
    }

    private Optional<JsonNode> findParticipantByPuuId(JsonNode participantsNode, String puuId) {
        return StreamSupport.stream(participantsNode.spliterator(), false)
                .filter(p -> p.has("puuid") && p.get("puuid").asText().equals(puuId))
                .findFirst();
    }

    private Optional<Integer> getPlacementIfMatchModeWithMatchId(String matchId, String gameMode, Summoner summoner) {
        try {
            String details = getMatchDetails(matchId);
            return getPlacement(gameMode, summoner, details);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error while processing JSON for match " + matchId, e);
        }
    }

    private Optional<Integer> getPlacementIfMatchModeAndSetWithMatchId(String matchId, String gameMode, Summoner summoner, String set) {
        try {
            Optional<String> details = getMatchDetailsIfSet(matchId, set);
            if (details.isPresent()) {
                return getPlacement(gameMode, summoner, details.get());
            } else {
                return Optional.empty();
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error while processing JSON for match " + matchId, e);
        }
    }
}
