package com.p1h.p1htactics.dto;

import java.util.List;

public record EventData(List<SummonerAvgEventResult> avgResults, List<PlacementCountDto> placementCounts) {}
