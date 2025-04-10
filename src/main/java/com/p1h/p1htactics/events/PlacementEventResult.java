package com.p1h.p1htactics.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlacementEventResult extends EventResult {
    private long top;
    private long bottom;

    public PlacementEventResult(String summonerName, int games, long top, long bottom) {
        super(summonerName, games);
        this.top = top;
        this.bottom = bottom;
    }
}
