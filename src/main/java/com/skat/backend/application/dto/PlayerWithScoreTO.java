package com.skat.backend.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PlayerWithScoreTO(
	UUID id,
	String first_name,
	String last_name,
	int current_total_points,
	int current_sequence_index,
	OffsetDateTime updated_at) {
}
