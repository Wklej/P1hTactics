package com.p1h.p1htactics.events;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    private String title;
    private LocalDate start;
    private LocalDate end;
    private List<EventResult> eventResults;
//  private boolean active;
}
