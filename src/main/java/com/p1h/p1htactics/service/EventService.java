package com.p1h.p1htactics.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.p1h.p1htactics.entity.EventEntity;
import com.p1h.p1htactics.entity.Match;
import com.p1h.p1htactics.entity.Summoner;
import com.p1h.p1htactics.events.*;
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

    public EventEntity getEvent(String eventTitle) {
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
        var event = new EventEntity(null, title, start, end, null, true);
        eventRepository.save(event);
    }

    public void addParticipantToEvent(String title, String username) {
        var event = eventRepository.findEventByTitle(title.toLowerCase());
        event.getParticipants().add(username);
        eventRepository.save(event);
    }

    public Map<String, Event> getAllEventResults() {
        var events = eventRepository.findAll();
        var eventsMap = new HashMap<String, Event>();

        for (var event : events) {
            var title = event.getTitle();

            if ("AVG".equalsIgnoreCase(title)) {
                eventsMap.put(title, getAvgEventResults(title));
            }
            else if ("TOP/BOTTOM".equalsIgnoreCase(title)) {
                eventsMap.put(title, getPlacementCountsForEvent(title));
            }
        }

        return eventsMap;
    }

    private Event getAvgEventResults(String eventTitle) {
        var event = this.getEvent(eventTitle);
        var participants = this.getParticipants(eventTitle);
        var signedSummoners = userRepository.findAll().stream()
                .filter(summoner -> participants.contains(summoner.getUsername()))
                .toList();

        var avgEvent = new Event(eventTitle, event.getStart(), event.getEnd(), new ArrayList<>());

        signedSummoners
                .forEach(summoner -> {
                    var validMatches = getMatchDetailsBetweenTime(
                            summoner.getMatchHistory(),
                            event.getStart(),
                            event.getEnd(),
                            summoner.getGameName());

                    // need to filter out OR db return by summonerName filter also
                    //validMatches.stream().distinct().toList();

                    var validRankedPlacements = getValidRankedPlacements(validMatches, summoner);

                    avgEvent.getEventResults().add(new AvgEventResult(
                            summoner.getUsername(),
                            validRankedPlacements.size(),
                            calculateAvgPlacementForEvent(validRankedPlacements)));
                });
        return avgEvent;
    }

    private Event getPlacementCountsForEvent(String eventTitle) {
        var event = this.getEvent(eventTitle);
        var participants = getParticipants(eventTitle);
        var signedSummoners = userRepository.findAll().stream()
                .filter(summoner -> participants.contains(summoner.getUsername()))
                .toList();

        var placementEvent = new Event(eventTitle, event.getStart(), event.getEnd(), new ArrayList<>());

        signedSummoners
                .forEach(summoner -> {
                    var validMatches = getMatchDetailsBetweenTime(
                            summoner.getMatchHistory(),
                            event.getStart(),
                            event.getEnd(),
                            summoner.getGameName()
                    );

                    var validRankedPlacements = getValidRankedPlacements(validMatches, summoner);
                    long topPlacementCount = getTopPlacementCount(validRankedPlacements);
                    long bottomPlacementCount = getBottomPlacementCount(validRankedPlacements);

                    placementEvent.getEventResults().add(new PlacementEventResult(
                            summoner.getUsername(),
                            validRankedPlacements.size(),
                            topPlacementCount,
                            bottomPlacementCount
                    ));
                });

        return placementEvent;
    }

    private List<String> getMatchDetailsBetweenTime(List<String> matchId, LocalDate start, LocalDate end, String summonerName) {
        var eventStart = LocalDateTime.of(start, LocalTime.MIN);
        var eventEnd = LocalDateTime.of(end, LocalTime.MIN);
        return matchRepository.findByMatchIdInAndGameTimeBetweenAndSummonerName(matchId, eventStart, eventEnd, summonerName).stream()
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
