package com.p1h.p1htactics.mapper;

import com.p1h.p1htactics.dto.RankedStatsDto;
import com.p1h.p1htactics.dto.SummonerDto;
import com.p1h.p1htactics.dto.SummonerRankingDto;
import com.p1h.p1htactics.dto.SummonerRankingStats;
import com.p1h.p1htactics.entity.Summoner;

public class SummonerMapper {

    public static SummonerDto summonerToSummonerDto(Summoner summoner) {
        return new SummonerDto(summoner.getGameName(), summoner.getTag());
    }

    public static SummonerRankingDto summonerDtoToSummonerRankingDto(SummonerDto summonerDto, double avg) {
        return new SummonerRankingDto(summonerDto.gameName(), avg);
    }

    public static SummonerRankingStats RankedStatsDtoToSummonerRankingStats(RankedStatsDto statsDto) {
        return new SummonerRankingStats(statsDto.rank(), statsDto.tier(), statsDto.leaguePoints());
    }
}
