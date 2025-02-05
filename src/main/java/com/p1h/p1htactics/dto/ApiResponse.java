package com.p1h.p1htactics.dto;

public record ApiResponse<T>(T data, String error) {}
