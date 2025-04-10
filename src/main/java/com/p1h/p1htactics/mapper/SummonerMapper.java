package com.p1h.p1htactics.mapper;

import com.p1h.p1htactics.dto.*;
import com.p1h.p1htactics.entity.EventEntity;
import com.p1h.p1htactics.entity.Summoner;
import com.p1h.p1htactics.events.AvgEvent;
import com.p1h.p1htactics.events.AvgEventResult;

import java.util.List;

public class SummonerMapper {

    public static SummonerDto summonerToSummonerDto(Summoner summoner) {
        return new SummonerDto(summoner.getGameName(), summoner.getTag());
    }

    public static SummonerRankingDto summonerDtoToSummonerRankingDto(SummonerDto summonerDto, double avg) {
        return new SummonerRankingDto(summonerDto.gameName(), avg);
    }

    public static SummonerAvgEventResult summonerToResultDto(Summoner summoner, double avg, int gamesCount, EventEntity event) {
        return new SummonerAvgEventResult(
                summoner.getUsername(),
                avg,
                gamesCount,
                new EventDto(
                        event.getTitle(),
                        event.getStart(),
                        event.getEnd(),
                        event.getParticipants()));
    }

    public static AvgEvent summonerToAvgEvent(Summoner summoner, double avg, int gamesCount, EventEntity event) {
        return new AvgEvent(
                event.getTitle(),
                event.getStart(),
                event.getEnd(),
                List.of(new AvgEventResult(summoner.getUsername(), gamesCount, avg)));
    }

    public static SummonerRankingStats RankedStatsDtoToSummonerRankingStats(RankedStatsDto statsDto) {
        return new SummonerRankingStats(statsDto.rank(), statsDto.tier(), statsDto.leaguePoints());
    }
}
