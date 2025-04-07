package com.p1h.p1htactics.dto;

public record RankedStatsDto(
        String puuid,
        String leagueId,
        String queueType,
        String tier,
        String rank,
        String summonerId,
        int leaguePoints,
        int wins,
        int losses,
        boolean veteran,
        boolean inactive,
        boolean freshBlood,
        boolean hotStreak
) {}
