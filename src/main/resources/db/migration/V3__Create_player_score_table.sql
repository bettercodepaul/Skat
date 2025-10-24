-- Table: player_score
-- Description: Stores cumulative scores for players across games
-- Each record represents a player's score at a specific point in the game sequence

CREATE TABLE player_score (
    -- Unique identifier for the score record
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Reference to the player (optional, can be nullified)
    player_id UUID,
    
    -- Reference to the game (required)
    game_id UUID NOT NULL,
    
    -- Sequence index indicating the order of this score in the series (required)
    sequence_index INTEGER NOT NULL,
    
    -- Total cumulative points for the player at this point (optional)
    total_points INTEGER,
    
    -- Timestamp when this score was recorded (required)
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    
    -- Foreign key constraints
    CONSTRAINT player_score_player_FK FOREIGN KEY (player_id) REFERENCES player(id),
    CONSTRAINT player_score_game_FK FOREIGN KEY (game_id) REFERENCES game(id)
);

-- Index on player_id for faster player score lookups
CREATE INDEX player_score_player_IDX ON player_score(player_id);

-- Index on game_id for faster game score lookups
CREATE INDEX player_score_game_IDX ON player_score(game_id);

-- Index on sequence_index for ordered queries
CREATE INDEX player_score_sequence_IDX ON player_score(sequence_index);

-- Add comments to columns
COMMENT ON TABLE player_score IS 'Stores cumulative scores for players across games';
COMMENT ON COLUMN player_score.id IS 'Unique identifier for the score record';
COMMENT ON COLUMN player_score.player_id IS 'Reference to the player (optional, can be nullified)';
COMMENT ON COLUMN player_score.game_id IS 'Reference to the game (required)';
COMMENT ON COLUMN player_score.sequence_index IS 'Sequence index indicating the order of this score in the series';
COMMENT ON COLUMN player_score.total_points IS 'Total cumulative points for the player at this point';
COMMENT ON COLUMN player_score.created_at IS 'Timestamp when this score was recorded';
