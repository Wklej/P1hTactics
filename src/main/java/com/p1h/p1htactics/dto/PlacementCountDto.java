package com.p1h.p1htactics.dto;

public record PlacementCountDto(String summonerName,
                                long top,
                                long bottom,
                                int games,
                                EventDto eventInfo) {
}
