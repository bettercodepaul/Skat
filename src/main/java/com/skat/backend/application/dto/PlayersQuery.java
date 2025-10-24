package com.skat.backend.application.dto;

public record PlayersQuery(
	int startIndex,
	int pageSize,
	PlayersSort sort) {
}
