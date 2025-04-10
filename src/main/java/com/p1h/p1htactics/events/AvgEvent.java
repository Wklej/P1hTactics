package com.p1h.p1htactics.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AvgEvent extends Event {
    private List<EventResult> avgEventResults;

    public AvgEvent(String title, LocalDate start, LocalDate end, List<EventResult> avgEventResults) {
        super(title, start, end);
        this.avgEventResults = avgEventResults;
    }
}
