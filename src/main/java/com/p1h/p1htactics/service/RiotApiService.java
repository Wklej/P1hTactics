package com.p1h.p1htactics.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.p1h.p1htactics.repository.MatchRepository;
import com.p1h.p1htactics.repository.UserRepository;
import com.p1h.p1htactics.util.WebClientProxy;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    public double getAvgPlacement(String gameName, String tagLine) {
        var summoner = userRepository.findSummonerByGameNameAndTag(gameName, tagLine).orElseThrow();
        var matchHistory = summoner.getMatchHistory();

        return matchHistory.stream()
                .mapToDouble(matchId -> {
                    try {
                        var details = getMatchDetails(matchId);
                        var jsonNode = objectMapper.readTree(details);
                        var participantsNode = jsonNode.get("info").get("participants");
                        return findParticipantByPuuId(participantsNode, summoner.getPuuid())
                                .map(p -> p.get("placement").asInt())
                                .orElseThrow();
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Error while processing JSON: ", e);
                    }
                })
                .average()
                .stream()
                .map(avg -> BigDecimal.valueOf(avg)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue())
                .findFirst()
                .orElse(0.0);
    }

    private String getMatchDetails(String matchId) {
        return matchRepository.findMatchByMatchId(matchId).getDetails();
    }

    private Optional<JsonNode> findParticipantByPuuId(JsonNode participantsNode, String puuId) {
        return StreamSupport.stream(participantsNode.spliterator(), false)
                .filter(p -> p.has("puuid") && p.get("puuid").asText().equals(puuId))
                .findFirst();
    }
}
