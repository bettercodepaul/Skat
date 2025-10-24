-- Table: player
-- Description: Stores player information for the Skat game
-- Each player has a unique combination of first_name and last_name

CREATE TABLE player (
    -- Unique identifier for the player
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Player's first name (required, max 50 characters)
    first_name VARCHAR NOT NULL,
    
    -- Player's last name (required, max 50 characters)
    last_name VARCHAR NOT NULL,
    
    -- Ensure unique combination of first and last name
    CONSTRAINT player_first_last_name_UQ UNIQUE (first_name, last_name)
);

-- Index on first_name for faster lookups
CREATE INDEX player_first_name_IDX ON player(first_name);

-- Index on last_name for faster lookups
CREATE INDEX player_last_name_IDX ON player(last_name);

-- Add comments to columns
COMMENT ON TABLE player IS 'Stores player information for the Skat game';
COMMENT ON COLUMN player.id IS 'Unique identifier for the player';
COMMENT ON COLUMN player.first_name IS 'Player''s first name (required, max 50 characters)';
COMMENT ON COLUMN player.last_name IS 'Player''s last name (required, max 50 characters)';
