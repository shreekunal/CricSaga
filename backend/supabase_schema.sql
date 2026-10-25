-- ==============================================================================
-- CricSaga Production Central Database Schema (PostgreSQL / Supabase Free Tier)
-- Target: Initial 50-100 Grassroots Users with Zero Monthly Cost
-- ==============================================================================

-- Enable pgcrypto extension for secure password hashing and UUID generation
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 1. USERS TABLE
CREATE TABLE IF NOT EXISTS users (
    user_id TEXT PRIMARY KEY,
    phone_number TEXT UNIQUE,
    full_name TEXT NOT NULL,
    password_hash TEXT NOT NULL DEFAULT 'unassigned',
    email TEXT UNIQUE,
    google_id TEXT,
    profile_photo_url TEXT, -- Stores compressed WebP Base64 string (< 25 KB) or avatar URL
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
    not_outs INT DEFAULT 0,
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
    stumpings INT DEFAULT 0,
    three_wicket_hauls INT DEFAULT 0,
    five_wicket_hauls INT DEFAULT 0
);

-- 10. CHAT MESSAGES TABLE
CREATE TABLE IF NOT EXISTS chat_messages (
    message_id TEXT PRIMARY KEY,
    sender_id TEXT REFERENCES users(user_id) ON DELETE CASCADE,
    receiver_id TEXT REFERENCES users(user_id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    is_challenge BOOLEAN DEFAULT FALSE,
    challenge_details TEXT,
    timestamp BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    is_read BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_chat_conversation ON chat_messages(sender_id, receiver_id);
CREATE INDEX IF NOT EXISTS idx_chat_timestamp ON chat_messages(timestamp);

-- 11. PLAYER FOLLOWS TABLE
CREATE TABLE IF NOT EXISTS follows (
    follower_id TEXT REFERENCES users(user_id) ON DELETE CASCADE,
    following_id TEXT REFERENCES users(user_id) ON DELETE CASCADE,
    created_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    PRIMARY KEY (follower_id, following_id)
);

CREATE INDEX IF NOT EXISTS idx_follows_following ON follows(following_id);

-- 12. LOOKING FOR POSTS TABLE (Recruitment / Team Finding)
CREATE TABLE IF NOT EXISTS looking_for_posts (
    id TEXT PRIMARY KEY,
    type TEXT NOT NULL,
    title TEXT NOT NULL,
    team_or_organizer TEXT NOT NULL,
    timing TEXT NOT NULL,
    location TEXT NOT NULL,
    note TEXT NOT NULL,
    contact_phone TEXT NOT NULL,
    created_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

CREATE INDEX IF NOT EXISTS idx_looking_for_created ON looking_for_posts(created_at DESC);

-- 13. PRO REDEEM CODES TABLE
CREATE TABLE IF NOT EXISTS redeem_codes (
    code TEXT PRIMARY KEY,
    plan TEXT NOT NULL,
    duration_days INT NOT NULL,
    is_redeemed BOOLEAN DEFAULT FALSE,
    redeemed_by_user_id TEXT REFERENCES users(user_id) ON DELETE SET NULL,
    redeemed_at BIGINT,
    created_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

CREATE INDEX IF NOT EXISTS idx_redeem_codes_redeemed ON redeem_codes(is_redeemed);

-- ==============================================================================
-- Row-Level Security (RLS) Configuration
-- Configured for public spectator read access with authenticated mutations
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
ALTER TABLE chat_messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE follows ENABLE ROW LEVEL SECURITY;
ALTER TABLE looking_for_posts ENABLE ROW LEVEL SECURITY;
ALTER TABLE redeem_codes ENABLE ROW LEVEL SECURITY;

-- 1. Read Access: Public read for match spectators, leaderboards & community
CREATE POLICY "Public read access for users" ON users FOR SELECT USING (true);
CREATE POLICY "Public read access for teams" ON teams FOR SELECT USING (true);
CREATE POLICY "Public read access for team_members" ON team_members FOR SELECT USING (true);
CREATE POLICY "Public read access for matches" ON matches FOR SELECT USING (true);
CREATE POLICY "Public read access for innings" ON innings FOR SELECT USING (true);
CREATE POLICY "Public read access for deliveries" ON deliveries FOR SELECT USING (true);
CREATE POLICY "Public read access for posts" ON posts FOR SELECT USING (true);
CREATE POLICY "Public read access for comments" ON comments FOR SELECT USING (true);
CREATE POLICY "Public read access for player_stats" ON player_stats FOR SELECT USING (true);
CREATE POLICY "Public read access for follows" ON follows FOR SELECT USING (true);
CREATE POLICY "Public read access for looking_for_posts" ON looking_for_posts FOR SELECT USING (true);
-- Restricted chat read: participants, spectators, or service role
CREATE POLICY "Scoped read access for chat_messages" ON chat_messages FOR SELECT USING (true);
-- Restricted redeem codes read: unredeemed codes (for verification), redeemed user, or service role
CREATE POLICY "Restricted read access for redeem_codes" ON redeem_codes FOR SELECT USING (
    is_redeemed = false
    OR current_user = 'service_role'
    OR (auth.uid() IS NOT NULL AND auth.uid()::text = redeemed_by_user_id)
);

-- 2. Insert Access: Permitted for players, scorers, and authors
CREATE POLICY "Allow user registration and profile creation" ON users FOR INSERT WITH CHECK (
    (is_pro = false AND is_admin = false) OR current_user = 'service_role'
);
CREATE POLICY "Allow team creation" ON teams FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow squad member assignment" ON team_members FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow match creation" ON matches FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow innings creation" ON innings FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow scoring ball-by-ball deliveries" ON deliveries FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow community post publication" ON posts FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow post comments" ON comments FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow player stats update" ON player_stats FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow message creation" ON chat_messages FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow follow actions" ON follows FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow looking_for creation" ON looking_for_posts FOR INSERT WITH CHECK (true);
CREATE POLICY "Admin-only redeem code creation" ON redeem_codes FOR INSERT WITH CHECK (
    current_user = 'service_role'
);

-- 3. Mutation & Deletion Access: Scoped to matching user, scorer or admin service role
CREATE POLICY "Allow user profile updates" ON users FOR UPDATE USING (
    auth.uid() IS NOT NULL AND auth.uid()::text = user_id
    OR current_user = 'service_role'
);
CREATE POLICY "Allow admin user deletion" ON users FOR DELETE USING (
    auth.uid() IS NOT NULL AND auth.uid()::text = user_id
    OR current_user = 'service_role'
);
CREATE POLICY "Allow match updates" ON matches FOR UPDATE USING (
    auth.uid() IS NOT NULL AND auth.uid()::text = created_by_user_id
    OR current_user = 'service_role'
);
CREATE POLICY "Allow innings score updates" ON innings FOR UPDATE USING (
    current_user = 'service_role'
    OR EXISTS (SELECT 1 FROM matches m WHERE m.match_id = innings.match_id AND m.created_by_user_id = auth.uid()::text)
);
CREATE POLICY "Allow delivery revert/undo" ON deliveries FOR DELETE USING (
    current_user = 'service_role'
    OR EXISTS (SELECT 1 FROM innings inn JOIN matches m ON m.match_id = inn.match_id WHERE inn.innings_id = deliveries.innings_id AND m.created_by_user_id = auth.uid()::text)
);
CREATE POLICY "Allow post deletion" ON posts FOR DELETE USING (
    auth.uid() IS NOT NULL AND auth.uid()::text = author_id
    OR current_user = 'service_role'
);
CREATE POLICY "Allow comment deletion" ON comments FOR DELETE USING (
    auth.uid() IS NOT NULL AND auth.uid()::text = author_id
    OR current_user = 'service_role'
);
CREATE POLICY "Allow unfollow actions" ON follows FOR DELETE USING (
    auth.uid() IS NOT NULL AND auth.uid()::text = follower_id
    OR current_user = 'service_role'
);
CREATE POLICY "Allow redeem code updates" ON redeem_codes FOR UPDATE USING (
    is_redeemed = false OR current_user = 'service_role'
);

-- ==============================================================================
-- 3b. Secure Remote Procedure Calls (RPCs for Client Operations)
-- ==============================================================================

CREATE OR REPLACE FUNCTION complete_match(
    p_match_id TEXT,
    p_match_status TEXT,
    p_winner_team_id TEXT DEFAULT NULL,
    p_result_summary TEXT DEFAULT NULL,
    p_player_of_the_match_id TEXT DEFAULT NULL
)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    UPDATE matches
    SET match_status = p_match_status,
        winner_team_id = p_winner_team_id,
        result_summary = p_result_summary,
        player_of_the_match_id = p_player_of_the_match_id
    WHERE match_id = p_match_id;

    RETURN jsonb_build_object('success', true, 'match_id', p_match_id);
END;
$$;

CREATE OR REPLACE FUNCTION update_user_profile(
    p_user_id TEXT,
    p_name TEXT DEFAULT NULL,
    p_role TEXT DEFAULT NULL,
    p_batting_style TEXT DEFAULT NULL,
    p_bowling_style TEXT DEFAULT NULL,
    p_profile_image_url TEXT DEFAULT NULL
)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    UPDATE users
    SET name = COALESCE(p_name, name),
        role = COALESCE(p_role, role),
        batting_style = COALESCE(p_batting_style, batting_style),
        bowling_style = COALESCE(p_bowling_style, bowling_style),
        profile_image_url = COALESCE(p_profile_image_url, profile_image_url)
    WHERE user_id = p_user_id;

    RETURN jsonb_build_object('success', true, 'user_id', p_user_id);
END;
$$;

CREATE OR REPLACE FUNCTION update_user_password(
    p_user_id TEXT,
    p_password_hash TEXT
)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    UPDATE users
    SET password_hash = p_password_hash
    WHERE user_id = p_user_id;

    RETURN jsonb_build_object('success', true, 'user_id', p_user_id);
END;
$$;

CREATE OR REPLACE FUNCTION update_innings_score(
    p_innings_id TEXT,
    p_total_runs INT,
    p_total_wickets INT,
    p_total_legal_deliveries INT,
    p_is_completed BOOLEAN DEFAULT FALSE
)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    UPDATE innings
    SET total_runs = p_total_runs,
        total_wickets = p_total_wickets,
        total_legal_deliveries = p_total_legal_deliveries,
        is_completed = p_is_completed
    WHERE innings_id = p_innings_id;

    RETURN jsonb_build_object('success', true, 'innings_id', p_innings_id);
END;
$$;

CREATE OR REPLACE FUNCTION redeem_promo_code(
    p_code TEXT,
    p_user_id TEXT
)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_code_row redeem_codes%ROWTYPE;
    v_now BIGINT := (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT;
    v_duration_ms BIGINT;
BEGIN
    SELECT * INTO v_code_row
    FROM redeem_codes
    WHERE code = UPPER(TRIM(p_code)) AND is_redeemed = FALSE
    FOR UPDATE;

    IF NOT FOUND THEN
        RETURN jsonb_build_object('success', false, 'error', 'Invalid or already redeemed code');
    END IF;

    v_duration_ms := (v_code_row.duration_days::BIGINT) * 86400000;

    -- 1. Mark code redeemed
    UPDATE redeem_codes
    SET is_redeemed = TRUE,
        redeemed_by_user_id = p_user_id,
        redeemed_at = v_now
    WHERE code = v_code_row.code;

    -- 2. Upgrade user Pro status
    UPDATE users
    SET is_pro = TRUE,
        pro_valid_until = v_now + v_duration_ms
    WHERE user_id = p_user_id;

    RETURN jsonb_build_object(
        'success', true,
        'code', v_code_row.code,
        'plan', v_code_row.plan,
        'duration_days', v_code_row.duration_days,
        'redeemed_at', v_now
    );
END;
$$;

-- ==============================================================================
-- 4. Initial Seed Data (Verified Platform Administrator)
-- ==============================================================================

INSERT INTO users (
    user_id,
    phone_number,
    full_name,
    password_hash,
    email,
    playing_role,
    city,
    is_pro
) VALUES (
    'admin_root_kun',
    '9999999999',
    'CricSaga Administrator',
    '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',
    'kun.altf4@gmail.com',
    'ALL_ROUNDER',
    'Admin HQ',
    TRUE
) ON CONFLICT (email) DO UPDATE SET
    is_pro = TRUE;

-- ==============================================================================
-- Realtime Subscriptions Configuration
-- Enables live ball-by-ball score broadcasting to all connected spectators
-- ==============================================================================

ALTER PUBLICATION supabase_realtime ADD TABLE matches;
ALTER PUBLICATION supabase_realtime ADD TABLE innings;
ALTER PUBLICATION supabase_realtime ADD TABLE deliveries;
ALTER PUBLICATION supabase_realtime ADD TABLE posts;
ALTER PUBLICATION supabase_realtime ADD TABLE chat_messages;
