package com.p1h.p1htactics.service;

import com.p1h.p1htactics.entity.Event;
import com.p1h.p1htactics.repository.EventRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class EventService {
    private final EventRepository eventRepository;

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
}
