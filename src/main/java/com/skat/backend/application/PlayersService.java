package com.skat.backend.application;

import com.skat.backend.application.dto.PlayerListResponseTO;
import com.skat.backend.application.dto.PlayerTO;
import com.skat.backend.application.dto.PlayersQuery;
import com.skat.backend.application.dto.UpsertPlayerRequest;
import java.util.UUID;

public interface PlayersService {

	PlayerListResponseTO listPlayers(PlayersQuery query);

	PlayerTO createPlayer(UpsertPlayerRequest request);

	PlayerTO updatePlayer(UUID id, UpsertPlayerRequest request);

	void deletePlayer(UUID id, boolean forceDeletion);
}
