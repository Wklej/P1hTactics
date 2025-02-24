package com.p1h.p1htactics.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.p1h.p1htactics.dto.ResultDto;
import com.p1h.p1htactics.dto.SummonerRankingDto;
import com.p1h.p1htactics.entity.Match;
import com.p1h.p1htactics.entity.Summoner;
import com.p1h.p1htactics.mapper.SummonerMapper;
import com.p1h.p1htactics.repository.MatchRepository;
import com.p1h.p1htactics.repository.UserRepository;
import com.p1h.p1htactics.util.WebClientProxy;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
@AllArgsConstructor
public class RiotApiService {

    private final WebClientProxy webClientProxy;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final EventService eventService;

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

    public List<SummonerRankingDto> getRankings() {
        var summoners = userRepository.findAll().stream()
                .map(SummonerMapper::summonerToSummonerDto)
                .toList();

        return summoners.stream()
                .map(summonerDto -> SummonerMapper.summonerDtoToSummonerRankingDto(
                        summonerDto,
                        getAvgPlacement(summonerDto.gameName(), summonerDto.tag(), "1100", 1000)))
                .toList();
    }

    public List<ResultDto> getEventResults(String eventTitle) {
        var event = eventService.getEvent(eventTitle);
        var participants = eventService.getParticipants(eventTitle);
        var signedSummoners = userRepository.findAll().stream()
                .filter(summoner -> participants.contains(summoner.getUsername()))
                .toList();

        return signedSummoners.stream()
                .map(summoner -> {
                    var validMatches = getMatchDetailsBetweenTime(
                            summoner.getMatchHistory(),
                            event.getStart(),
                            event.getEnd());

                    double avgPlacement = validMatches.stream()
                            .map(matchDetails -> getPlacementIfMatchModeWithDetails(matchDetails, "1100", summoner))
                            .flatMap(Optional::stream)
                            .mapToInt(Integer::intValue)
                            .average()
                            .orElse(0.0);

                    int gamesCount = validMatches.size();

                    return new ResultDto(summoner.getUsername(), avgPlacement, gamesCount);
                })
                .toList();
    }

    private String getMatchDetails(String matchId) {
        return matchRepository.findMatchByMatchId(matchId).getDetails();
    }

    private List<String> getMatchDetailsBetweenTime(List<String> matchId, LocalDate start, LocalDate end) {
        var eventStart = LocalDateTime.of(start, LocalTime.MIN);
        var eventEnd = LocalDateTime.of(end, LocalTime.MIN);
        return matchRepository.findByMatchIdInAndGameTimeBetween(matchId, eventStart, eventEnd).stream()
                .map(Match::getDetails)
                .toList();
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

    private Optional<Integer> getPlacementIfMatchModeWithDetails(String details, String gameMode, Summoner summoner) {
        try {
            return getPlacement(gameMode, summoner, details);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error while processing JSON for match " + details, e);
        }
    }

    private Optional<Integer> getPlacement(String gameMode, Summoner summoner, String details) throws JsonProcessingException {
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
}
