package com.p1h.p1htactics.controller;

import com.p1h.p1htactics.dto.SignUpDto;
import com.p1h.p1htactics.events.Event;
import com.p1h.p1htactics.service.EventService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/event/addParticipant")
    public void addParticipant(@RequestBody SignUpDto signUpDto) {
        eventService.addParticipantToEvent(signUpDto.title(), signUpDto.username());
    }

    @GetMapping("/api/getEventResults")
    public Map<String, Event> getEventResults() {
        return eventService.getAllEventResults();
    }
}
