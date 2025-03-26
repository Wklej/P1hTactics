package com.p1h.p1htactics.dto;

import java.time.LocalDate;
import java.util.List;

public record EventDto(String title,
                       LocalDate start,
                       LocalDate end,
                       List<String> participants) {}
