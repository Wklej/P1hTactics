package com.p1h.p1htactics.mapper;

import com.p1h.p1htactics.dto.SummonerDto;
import com.p1h.p1htactics.entity.Summoner;

public class SummonerMapper {

    public static SummonerDto summonerToSummonerDto(Summoner summoner) {
        return new SummonerDto(summoner.getGameName(), summoner.getTag());
    }
}
