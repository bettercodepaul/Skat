package com.skat.backend.application;

import com.skat.backend.application.dto.*;

import java.util.UUID;

public interface PlayersService {
    
    PlayerListResponseTO listPlayers(PlayersQuery query);
    
    PlayerTO createPlayer(UpsertPlayerRequest request);
    
    PlayerTO updatePlayer(UUID id, UpsertPlayerRequest request);
    
    void deletePlayer(UUID id, boolean forceDeletion);
}
