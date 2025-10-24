package com.skat.backend.application.dto;

import java.util.List;

public record PlayerListResponseTO(
	List<PlayerWithScoreTO> items,
	PagingTO paging,
	PlayersSort sort) {
}
