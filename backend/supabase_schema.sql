-- ==============================================================================
-- CricSaga Production Central Database Schema (PostgreSQL / Supabase Free Tier)
-- Target: Initial 50-100 Grassroots Users with Zero Monthly Cost
-- ==============================================================================

-- Enable pgcrypto extension for secure password hashing and UUID generation
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 1. USERS TABLE
CREATE TABLE IF NOT EXISTS users (
    user_id TEXT PRIMARY KEY,
    phone_number TEXT UNIQUE NOT NULL,
    full_name TEXT NOT NULL,
    password_hash TEXT NOT NULL,
    profile_photo_url TEXT, -- Stores compressed WebP Base64 string (< 25 KB)
    playing_role TEXT DEFAULT 'ALL_ROUNDER',
    batting_style TEXT DEFAULT 'RIGHT_HAND',
    bowling_style TEXT DEFAULT 'RIGHT_ARM_MEDIUM',
    city TEXT DEFAULT 'Mumbai',
    jersey_number TEXT DEFAULT '18',
    is_pro BOOLEAN DEFAULT FALSE,
    pro_plan TEXT, -- 'ONE_MONTH', 'ONE_YEAR', 'LIFETIME'
    pro_expires_at BIGINT DEFAULT 0,
    created_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone_number);

-- 2. TEAMS TABLE
CREATE TABLE IF NOT EXISTS teams (
    team_id TEXT PRIMARY KEY,
    team_name TEXT NOT NULL,
    short_code TEXT NOT NULL,
    city TEXT DEFAULT '',
    captain_id TEXT REFERENCES users(user_id) ON DELETE SET NULL,
    created_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

-- 3. TEAM MEMBERS TABLE
CREATE TABLE IF NOT EXISTS team_members (
    team_id TEXT REFERENCES teams(team_id) ON DELETE CASCADE,
    user_id TEXT REFERENCES users(user_id) ON DELETE CASCADE,
    jersey_number INT,
    joined_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    PRIMARY KEY (team_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_team_members_user ON team_members(user_id);

-- 4. MATCHES TABLE
CREATE TABLE IF NOT EXISTS matches (
    match_id TEXT PRIMARY KEY,
    match_title TEXT NOT NULL,
    team_a_id TEXT REFERENCES teams(team_id) ON DELETE RESTRICT,
    team_b_id TEXT REFERENCES teams(team_id) ON DELETE RESTRICT,
    total_overs INT DEFAULT 20,
    ball_type TEXT DEFAULT 'TENNIS_LIGHT',
    toss_winner_team_id TEXT,
    toss_decision TEXT, -- 'BAT' or 'BOWL'
    max_overs_per_bowler INT DEFAULT 4,
    is_lbw_enabled BOOLEAN DEFAULT FALSE,
    is_free_hit_enabled BOOLEAN DEFAULT TRUE,
    match_status TEXT DEFAULT 'SCHEDULED', -- 'SCHEDULED', 'LIVE', 'COMPLETED', 'ABANDONED'
    winner_team_id TEXT,
    result_summary TEXT,
    player_of_the_match_id TEXT,
    created_by_user_id TEXT REFERENCES users(user_id) ON DELETE SET NULL,
    created_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

CREATE INDEX IF NOT EXISTS idx_matches_status ON matches(match_status);
CREATE INDEX IF NOT EXISTS idx_matches_team_a ON matches(team_a_id);
CREATE INDEX IF NOT EXISTS idx_matches_team_b ON matches(team_b_id);

-- 5. INNINGS TABLE
CREATE TABLE IF NOT EXISTS innings (
    innings_id TEXT PRIMARY KEY,
    match_id TEXT REFERENCES matches(match_id) ON DELETE CASCADE,
    innings_number INT NOT NULL, -- 1 or 2
    batting_team_id TEXT REFERENCES teams(team_id) ON DELETE RESTRICT,
    bowling_team_id TEXT REFERENCES teams(team_id) ON DELETE RESTRICT,
    total_runs INT DEFAULT 0,
    total_wickets INT DEFAULT 0,
    total_legal_deliveries INT DEFAULT 0,
    target_runs INT,
    is_completed BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_innings_match ON innings(match_id);

-- 6. DELIVERIES TABLE (Ball-by-Ball)
CREATE TABLE IF NOT EXISTS deliveries (
    delivery_id TEXT PRIMARY KEY,
    innings_id TEXT REFERENCES innings(innings_id) ON DELETE CASCADE,
    delivery_index INT NOT NULL,
    over_number INT NOT NULL,
    ball_in_over INT NOT NULL,
    striker_id TEXT REFERENCES users(user_id) ON DELETE RESTRICT,
    non_striker_id TEXT REFERENCES users(user_id) ON DELETE RESTRICT,
    bowler_id TEXT REFERENCES users(user_id) ON DELETE RESTRICT,
    runs_off_bat INT DEFAULT 0,
    extra_type TEXT DEFAULT 'NONE',
    extra_runs INT DEFAULT 0,
    is_legal_delivery BOOLEAN DEFAULT TRUE,
    is_wicket BOOLEAN DEFAULT FALSE,
    dismissal_type TEXT,
    dismissed_player_id TEXT,
    primary_fielder_id TEXT,
    secondary_fielder_id TEXT,
    runs_completed_before_dismissal INT DEFAULT 0,
    next_striker_id_after_dismissal TEXT,
    timestamp BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    CONSTRAINT unique_innings_delivery_index UNIQUE (innings_id, delivery_index)
);

CREATE INDEX IF NOT EXISTS idx_deliveries_innings ON deliveries(innings_id);
CREATE INDEX IF NOT EXISTS idx_deliveries_bowler ON deliveries(bowler_id);
CREATE INDEX IF NOT EXISTS idx_deliveries_striker ON deliveries(striker_id);

-- 7. SOCIAL POSTS TABLE
CREATE TABLE IF NOT EXISTS posts (
    post_id TEXT PRIMARY KEY,
    author_id TEXT REFERENCES users(user_id) ON DELETE CASCADE,
    author_name TEXT NOT NULL,
    author_role TEXT NOT NULL,
    author_city TEXT NOT NULL,
    author_team_name TEXT,
    author_avatar_url TEXT,
    content TEXT NOT NULL,
    image_uri TEXT, -- Base64 WebP format (< 30 KB)
    linked_match_id TEXT,
    performance_tag TEXT,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    created_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

CREATE INDEX IF NOT EXISTS idx_posts_author ON posts(author_id);
CREATE INDEX IF NOT EXISTS idx_posts_created ON posts(created_at DESC);

-- 8. POST COMMENTS TABLE
CREATE TABLE IF NOT EXISTS comments (
    comment_id TEXT PRIMARY KEY,
    post_id TEXT REFERENCES posts(post_id) ON DELETE CASCADE,
    author_id TEXT REFERENCES users(user_id) ON DELETE CASCADE,
    author_name TEXT NOT NULL,
    author_avatar_url TEXT,
    text TEXT NOT NULL,
    created_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

CREATE INDEX IF NOT EXISTS idx_comments_post ON comments(post_id);

-- 9. PLAYER STATS AGGREGATE TABLE
CREATE TABLE IF NOT EXISTS player_stats (
    user_id TEXT PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    matches INT DEFAULT 0,
    innings INT DEFAULT 0,
    runs_scored INT DEFAULT 0,
    balls_faced INT DEFAULT 0,
    highest_score INT DEFAULT 0,
    fifties INT DEFAULT 0,
    hundreds INT DEFAULT 0,
    fours INT DEFAULT 0,
    sixes INT DEFAULT 0,
    wickets_taken INT DEFAULT 0,
    legal_balls_bowled INT DEFAULT 0,
    runs_conceded INT DEFAULT 0,
    maidens INT DEFAULT 0,
    catches INT DEFAULT 0,
    run_outs INT DEFAULT 0,
    stumpings INT DEFAULT 0
);

-- ==============================================================================
-- Row-Level Security (RLS) Configuration
-- Configured for easy access using Supabase Anon Public Key during launch
-- ==============================================================================

ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE teams ENABLE ROW LEVEL SECURITY;
ALTER TABLE team_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE matches ENABLE ROW LEVEL SECURITY;
ALTER TABLE innings ENABLE ROW LEVEL SECURITY;
ALTER TABLE deliveries ENABLE ROW LEVEL SECURITY;
ALTER TABLE posts ENABLE ROW LEVEL SECURITY;
ALTER TABLE comments ENABLE ROW LEVEL SECURITY;
ALTER TABLE player_stats ENABLE ROW LEVEL SECURITY;

-- Allow public read and write access for initial 50-100 grassroots players
CREATE POLICY "Public full access to users" ON users FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Public full access to teams" ON teams FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Public full access to team_members" ON team_members FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Public full access to matches" ON matches FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Public full access to innings" ON innings FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Public full access to deliveries" ON deliveries FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Public full access to posts" ON posts FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Public full access to comments" ON comments FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Public full access to player_stats" ON player_stats FOR ALL USING (true) WITH CHECK (true);

-- ==============================================================================
-- Realtime Subscriptions Configuration
-- Enables live ball-by-ball score broadcasting to all connected spectators
-- ==============================================================================

ALTER PUBLICATION supabase_realtime ADD TABLE matches;
ALTER PUBLICATION supabase_realtime ADD TABLE innings;
ALTER PUBLICATION supabase_realtime ADD TABLE deliveries;
ALTER PUBLICATION supabase_realtime ADD TABLE posts;
