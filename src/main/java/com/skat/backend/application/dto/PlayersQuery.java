package com.skat.backend.application.dto;

import java.util.UUID;

public record PlayersQuery(
    int startIndex,
    int pageSize,
    PlayersSort sort
) {}
