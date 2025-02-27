package com.p1h.p1htactics.controller;

import com.p1h.p1htactics.dto.EventData;
import com.p1h.p1htactics.dto.SummonerAvgEventResult;
import com.p1h.p1htactics.service.EventService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
public class EventController {
    private final EventService eventService;

    //TODO: switch to POST REQUESTS
    @GetMapping("/event/create/{title}/{start}/{end}")
    public ResponseEntity<?> createEvent(@PathVariable String title,
                                         @PathVariable String start,
                                         @PathVariable String end) {
        try {
            eventService.createEvent(title, start, end);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);
        }
    }

    @GetMapping("/event/addParticipant/{title}/{username}")
    public void addParticipant(@PathVariable String title, @PathVariable String username) {
        eventService.addParticipantToEvent(title, username);
    }

    @GetMapping("/api/getEventResults")
    public Map<String, EventData> getEventResults() {
        return eventService.getAllEventResults();
    }
}
