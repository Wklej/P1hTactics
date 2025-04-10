package com.p1h.p1htactics.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AvgEventResult extends EventResult {
    private double avg;

    public AvgEventResult(String summonerName, int games, double avg) {
        super(summonerName, games);
        this.avg = avg;
    }
}
