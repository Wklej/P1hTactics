package com.p1h.p1htactics.mapper;

import com.p1h.p1htactics.dto.EventDto;
import com.p1h.p1htactics.dto.SummonerAvgEventResult;
import com.p1h.p1htactics.dto.SummonerDto;
import com.p1h.p1htactics.dto.SummonerRankingDto;
import com.p1h.p1htactics.entity.Event;
import com.p1h.p1htactics.entity.Summoner;

public class SummonerMapper {

    public static SummonerDto summonerToSummonerDto(Summoner summoner) {
        return new SummonerDto(summoner.getGameName(), summoner.getTag());
    }

    public static SummonerRankingDto summonerDtoToSummonerRankingDto(SummonerDto summonerDto, double avg) {
        return new SummonerRankingDto(summonerDto.gameName(), avg);
    }

    public static SummonerAvgEventResult summonerToResultDto(Summoner summoner, double avg, int gamesCount, Event event) {
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
}
