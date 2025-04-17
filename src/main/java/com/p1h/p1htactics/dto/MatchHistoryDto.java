package com.p1h.p1htactics.dto;

import java.time.LocalDate;
import java.util.List;

public record MatchHistoryDto(String gameMode,
                              int placement,
                              List<Trait> traits,
                              LocalDate date) {}
