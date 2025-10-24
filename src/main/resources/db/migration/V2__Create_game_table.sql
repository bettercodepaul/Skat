-- Table: game
-- Description: Stores information about Skat games played
-- Each game can involve up to 3 players with one designated as the main player

CREATE TABLE game (
    -- Unique identifier for the game
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- First player in the game (optional, can be nullified)
    player1_id UUID,
    
    -- Second player in the game (optional, can be nullified)
    player2_id UUID,
    
    -- Third player in the game (optional, can be nullified)
    player3_id UUID,
    
    -- Main player who made the bid (optional, can be nullified)
    main_player_id UUID,
    
    -- Bid value for the game (optional)
    bid_value INTEGER,
    
    -- Score for the game (optional)
    score INTEGER,
    
    -- Timestamp when the game was played (required)
    played_at TIMESTAMP WITH TIME ZONE NOT NULL,
    
    -- Foreign key constraints (nullable to support player deletion)
    CONSTRAINT game_player1_FK FOREIGN KEY (player1_id) REFERENCES player(id),
    CONSTRAINT game_player2_FK FOREIGN KEY (player2_id) REFERENCES player(id),
    CONSTRAINT game_player3_FK FOREIGN KEY (player3_id) REFERENCES player(id),
    CONSTRAINT game_main_player_FK FOREIGN KEY (main_player_id) REFERENCES player(id)
);

-- Index on main_player_id for faster lookups
CREATE INDEX game_main_player_IDX ON game(main_player_id);

-- Index on played_at for chronological queries
CREATE INDEX game_played_at_IDX ON game(played_at);

-- Add comments to columns
COMMENT ON TABLE game IS 'Stores information about Skat games played';
COMMENT ON COLUMN game.id IS 'Unique identifier for the game';
COMMENT ON COLUMN game.player1_id IS 'First player in the game (optional, can be nullified)';
COMMENT ON COLUMN game.player2_id IS 'Second player in the game (optional, can be nullified)';
COMMENT ON COLUMN game.player3_id IS 'Third player in the game (optional, can be nullified)';
COMMENT ON COLUMN game.main_player_id IS 'Main player who made the bid (optional, can be nullified)';
COMMENT ON COLUMN game.bid_value IS 'Bid value for the game';
COMMENT ON COLUMN game.score IS 'Score for the game';
COMMENT ON COLUMN game.played_at IS 'Timestamp when the game was played';
