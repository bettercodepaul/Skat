package com.skat.backend.application.dto;

import java.util.UUID;

public record PlayerTO(
    UUID id,
    String first_name,
    String last_name
) {}
