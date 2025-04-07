package com.p1h.p1htactics.dto;

public record FriendDto(SummonerDto friend,
                        SummonerRankingStats rankedStats,
                        SummonerRankingStats doubleUpStats) {
}
