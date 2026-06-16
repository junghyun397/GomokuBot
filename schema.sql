CREATE TABLE IF NOT EXISTS announce (
    announce_id SERIAL PRIMARY KEY,
    contents text NOT NULL,
    create_date timestamp without time zone DEFAULT now()
);

CREATE TABLE IF NOT EXISTS channel_profile (
    channel_id UUID PRIMARY KEY,
    platform smallint NOT NULL,
    given_id bigint NOT NULL,
    name varchar NOT NULL,
    register_date timestamp without time zone DEFAULT now(),
    UNIQUE (given_id, platform)
);

CREATE TABLE IF NOT EXISTS channel_config (
    channel_id UUID PRIMARY KEY REFERENCES channel_profile (channel_id),
    language smallint NOT NULL,
    board_style smallint NOT NULL,
    focus_type smallint NOT NULL,
    swap_type smallint NOT NULL,
    archive_policy smallint NOT NULL,
    hint_type smallint NOT NULL,
    mark_type smallint NOT NULL
);

CREATE TABLE IF NOT EXISTS user_profile (
    user_id UUID PRIMARY KEY,
    platform smallint NOT NULL,
    given_id bigint NOT NULL,
    name varchar NOT NULL,
    unique_name varchar NOT NULL,
    profile_url varchar,
    announce_id int REFERENCES announce (announce_id),
    register_date timestamp without time zone DEFAULT now(),
    UNIQUE (given_id, platform)
);

CREATE TABLE IF NOT EXISTS user_rating (
    user_id UUID PRIMARY KEY REFERENCES user_profile (user_id),
    rating float NOT NULL
);

CREATE TABLE IF NOT EXISTS game_record (
    record_id SERIAL PRIMARY KEY,
    history smallint[] NOT NULL,
    cause smallint NOT NULL,
    win_color smallint,
    channel_id UUID NOT NULL REFERENCES channel_profile (channel_id),
    black_id UUID REFERENCES user_profile (user_id),
    white_id UUID REFERENCES user_profile (user_id),
    engine_level smallint,
    rule smallint NOT NULL,
    rating_delta float,
    create_date timestamp without time zone DEFAULT now(),
    CHECK (black_id IS NOT NULL OR white_id IS NOT NULL),
    CHECK ((black_id IS NULL OR white_id IS NULL) = (engine_level IS NOT NULL))
);

CREATE TABLE IF NOT EXISTS legacy_user_stats (
    user_id UUID PRIMARY KEY REFERENCES user_profile (user_id),
    black_wins int NOT NULL DEFAULT 0,
    black_losses int NOT NULL DEFAULT 0,
    black_draws int NOT NULL DEFAULT 0,
    white_wins int NOT NULL DEFAULT 0,
    white_losses int NOT NULL DEFAULT 0,
    white_draws int NOT NULL DEFAULT 0,
    last_update timestamp without time zone DEFAULT now()
);

CREATE OR REPLACE VIEW user_stats AS
WITH game_user_stats AS (
    SELECT
        user_id,
        (COUNT(*) FILTER (WHERE color = 0 AND win_color = 0))::int AS black_wins,
        (COUNT(*) FILTER (WHERE color = 0 AND win_color = 1))::int AS black_losses,
        (COUNT(*) FILTER (WHERE color = 0 AND win_color IS DISTINCT FROM 0 AND win_color IS DISTINCT FROM 1))::int AS black_draws,
        (COUNT(*) FILTER (WHERE color = 1 AND win_color = 1))::int AS white_wins,
        (COUNT(*) FILTER (WHERE color = 1 AND win_color = 0))::int AS white_losses,
        (COUNT(*) FILTER (WHERE color = 1 AND win_color IS DISTINCT FROM 0 AND win_color IS DISTINCT FROM 1))::int AS white_draws,
        MAX(create_date) AS last_update
    FROM (
        SELECT
            black_id AS user_id,
            0::smallint AS color,
            win_color,
            create_date
        FROM game_record
        WHERE engine_level IS NOT NULL
          AND black_id IS NOT NULL

        UNION ALL

        SELECT
            white_id AS user_id,
            1::smallint AS color,
            win_color,
            create_date
        FROM game_record
        WHERE engine_level IS NOT NULL
          AND white_id IS NOT NULL
    ) AS ai_game_record
    GROUP BY user_id
),
combined_user_stats AS (
    SELECT * FROM legacy_user_stats

    UNION ALL

    SELECT
        user_id,
        black_wins,
        black_losses,
        black_draws,
        white_wins,
        white_losses,
        white_draws,
        last_update
    FROM game_user_stats
)
SELECT
    user_id,
    SUM(black_wins)::int AS black_wins,
    SUM(black_losses)::int AS black_losses,
    SUM(black_draws)::int AS black_draws,
    SUM(white_wins)::int AS white_wins,
    SUM(white_losses)::int AS white_losses,
    SUM(white_draws)::int AS white_draws,
    MAX(last_update) AS last_update
FROM combined_user_stats
GROUP BY user_id;
