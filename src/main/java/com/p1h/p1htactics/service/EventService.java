package com.p1h.p1htactics.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.p1h.p1htactics.dto.ResultDto;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    /*
    getEventResults(title/type) {
        switch (title/type) {
            case "rank avg":  getRankAvgEventResults(title/type)
            case "topBottom": getTopBottomEventResults(title/type)
        }
    }

    scale to multiple event stats return (map?)
     */

    public List<ResultDto> getEventResults(String eventTitle) {
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

                    return SummonerMapper.summonerToResultDto(
                            summoner,
                            calculateAvgPlacementForEvent(summoner, validMatches),
                            validMatches.size(),
                            event);
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

    private double calculateAvgPlacementForEvent(Summoner summoner, List<String> validMatches) {
        return validMatches.stream()
                .map(matchDetails -> getPlacementIfMatchModeWithDetails(matchDetails, "1100", summoner))
                .flatMap(Optional::stream)
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
}
