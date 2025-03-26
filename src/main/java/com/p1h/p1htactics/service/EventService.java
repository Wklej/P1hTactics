package com.p1h.p1htactics.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.p1h.p1htactics.dto.EventData;
import com.p1h.p1htactics.dto.EventDto;
import com.p1h.p1htactics.dto.PlacementCountDto;
import com.p1h.p1htactics.dto.SummonerAvgEventResult;
import com.p1h.p1htactics.entity.Event;
import com.p1h.p1htactics.entity.Match;
import com.p1h.p1htactics.entity.Summoner;
import com.p1h.p1htactics.mapper.SummonerMapper;
import com.p1h.p1htactics.repository.EventRepository;
import com.p1h.p1htactics.repository.MatchRepository;
import com.p1h.p1htactics.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@AllArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final RiotApiService riotApiService;

    public Event getEvent(String eventTitle) {
        return eventRepository.findEventByTitle(eventTitle);
    }

    /**
     * Get list of usernames
     * @param eventTitle Event title
     * @return List of usernames
     */
    public List<String> getParticipants(String eventTitle) {
        return this.getEvent(eventTitle).getParticipants();
    }

    public void createEvent(String title, String eventStart, String eventEnd) {
        var start = LocalDate.parse(eventStart);
        var end = LocalDate.parse(eventEnd);
        var event = new Event(null, title, start, end, new ArrayList<>(), null, true);
        eventRepository.save(event);
    }

    public void addParticipantToEvent(String title, String username) {
        var event = eventRepository.findEventByTitle(title);
        event.getParticipants().add(username);
        eventRepository.save(event);
    }

    public Map<String, EventData> getAllEventResults() {
        var events = eventRepository.findAll();
        var eventsMap = new HashMap<String, EventData>();

        for (var event : events) {
            var title = event.getTitle();
            List<SummonerAvgEventResult> avgList = null;
            List<PlacementCountDto> placementList = null;

            if ("AVG".equalsIgnoreCase(title)) {
                avgList = getAvgEventResults(title);
            } else if ("TOP/BOTTOM".equalsIgnoreCase(title)) {
                placementList = getPlacementCountsForEvent(title);
            }

            eventsMap.put(title, new EventData(avgList, placementList));
        }

        return eventsMap;
    }

    private List<SummonerAvgEventResult> getAvgEventResults(String eventTitle) {
        var event = this.getEvent(eventTitle);
        var participants = this.getParticipants(eventTitle);
        var signedSummoners = userRepository.findAll().stream()
                .filter(summoner -> participants.contains(summoner.getUsername()))
                .toList();

        return signedSummoners.stream()
                .map(summoner -> {
                    var validMatches = getMatchDetailsBetweenTime(
                            summoner.getMatchHistory(),
                            event.getStart(),
                            event.getEnd());

                    var validRankedPlacements = getValidRankedPlacements(validMatches, summoner);

                    return SummonerMapper.summonerToResultDto(
                            summoner,
                            calculateAvgPlacementForEvent(validRankedPlacements),
                            validRankedPlacements.size(),
                            event);
                })
                .toList();
    }

    public List<PlacementCountDto> getPlacementCountsForEvent(String eventTitle) {
        var event = this.getEvent(eventTitle);
        var participants = getParticipants(eventTitle);
        var signedSummoners = userRepository.findAll().stream()
                .filter(summoner -> participants.contains(summoner.getUsername()))
                .toList();

        return signedSummoners.stream()
                .map(summoner -> {
                    var validMatches = getMatchDetailsBetweenTime(
                            summoner.getMatchHistory(),
                            event.getStart(),
                            event.getEnd()
                    );

                    var validRankedPlacements = getValidRankedPlacements(validMatches, summoner);
                    long topPlacementCount = getTopPlacementCount(validRankedPlacements);
                    long bottomPlacementCount = getBottomPlacementCount(validRankedPlacements);

                    return new PlacementCountDto(
                            summoner.getUsername(),
                            topPlacementCount,
                            bottomPlacementCount,
                            validRankedPlacements.size(),
                            new EventDto(event.getTitle(), event.getStart(), event.getEnd()));
                })
                .toList();
    }

    private List<String> getMatchDetailsBetweenTime(List<String> matchId, LocalDate start, LocalDate end) {
        var eventStart = LocalDateTime.of(start, LocalTime.MIN);
        var eventEnd = LocalDateTime.of(end, LocalTime.MIN);
        return matchRepository.findByMatchIdInAndGameTimeBetween(matchId, eventStart, eventEnd).stream()
                .map(Match::getDetails)
                .toList();
    }

    private List<Integer> getValidRankedPlacements(List<String> validMatches, Summoner summoner) {
        return validMatches.stream()
                .map(details -> getPlacementIfMatchModeWithDetails(details, "1100", summoner))
                .flatMap(Optional::stream)
                .toList();
    }

    private long getBottomPlacementCount(List<Integer> validRankedPlacements) {
        return validRankedPlacements.stream()
                .mapToInt(Integer::intValue)
                .filter(this::isBottom)
                .count();
    }

    private long getTopPlacementCount(List<Integer> validRankedPlacements) {
        return validRankedPlacements.stream()
                .mapToInt(Integer::intValue)
                .filter(this::isTop)
                .count();
    }

    private double calculateAvgPlacementForEvent(List<Integer> validRankedPlacements) {
        return validRankedPlacements.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
    }

    private Optional<Integer> getPlacementIfMatchModeWithDetails(String details, String gameMode, Summoner summoner) {
        try {
            return riotApiService.getPlacement(gameMode, summoner, details);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error while processing JSON for match " + details, e);
        }
    }

    private boolean isBottom(int placement) {
        return placement == 1 || placement == 2 || placement == 3;
    }

    private boolean isTop(int placement) {
        return placement == 8 || placement == 7 || placement == 6;
    }
}
