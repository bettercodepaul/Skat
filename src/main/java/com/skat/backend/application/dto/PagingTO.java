package com.skat.backend.application.dto;

public record PagingTO(
    int startIndex,
    int pageSize,
    long total
) {}
