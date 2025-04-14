package com.p1h.p1htactics.dto;

import java.util.List;

public record DuoResults(Duo duo, List<Integer> placements, double avg) {}
