CREATE TABLE announce (
    announce_id     SERIAL PRIMARY KEY,
    contents        TEXT NOT NULL,
    create_date     timestamptz DEFAULT now()
);

CREATE TABLE guild_profile (
    guild_id        uuid PRIMARY KEY,
    platform        SMALLINT NOT NULL,
    given_id        BIGINT NOT NULL,
    name            TEXT NOT NULL,
    register_date   timestamptz DEFAULT now(),

    UNIQUE (given_id, platform)
);

CREATE TABLE guild_config (
    guild_id        uuid PRIMARY KEY,
    language        SMALLINT NOT NULL,
    board_style     SMALLINT NOT NULL,
    focus_type      SMALLINT NOT NULL,
    swap_type       SMALLINT NOT NULL,
    archive_policy  SMALLINT NOT NULL,
    hint_type       SMALLINT NOT NULL,
    mark_type       SMALLINT NOT NULL,

    FOREIGN KEY (guild_id) REFERENCES guild_profile (guild_id)
);

CREATE TABLE user_profile (
    user_id uuid    PRIMARY KEY,
    platform        SMALLINT NOT NULL,
    given_id        BIGINT NOT NULL,
    name            TEXT NOT NULL,
    unique_name     TEXT NOT NULL,
    profile_url     TEXT,
    announce_id     INTEGER REFERENCES announce (announce_id),
    register_date   timestamptz DEFAULT now(),

    UNIQUE (given_id, platform)
);

CREATE TABLE game_record (
    record_id       SERIAL PRIMARY KEY,
    board_state     bytea NOT NULL,
    history         INTEGER[] NOT NULL,
    cause           SMALLINT NOT NULL,
    win_color       smallint,
    guild_id        uuid REFERENCES guild_profile (guild_id) NOT NULL,
    black_id        uuid REFERENCES user_profile (user_id),
    white_id        uuid REFERENCES user_profile (user_id),
    ai_level        SMALLINT,
    rule            SMALLINT,
    create_date     timestamptz DEFAULT now()
);

CREATE OR REPLACE VIEW user_stats AS
WITH finished_games AS (
    SELECT
        record_id,
        black_id,
        white_id,
        win_color,
        create_date
    FROM game_record
    WHERE
        black_id IS NOT NULL AND white_id IS NOT NULL
    -- 예: 완료된 게임만 집계하고 싶으면 여기서 cause 필터링
    -- AND cause IN ( ...완료 cause 목록... )
),
     per_user AS (
         -- black 관점 집계
         SELECT
             g.black_id AS user_id,
             SUM(CASE WHEN g.win_color = 0 THEN 1 ELSE 0 END)::int AS black_wins,
             SUM(CASE WHEN g.win_color = 1 THEN 1 ELSE 0 END)::int AS black_losses,
             0::int AS white_wins,
             0::int AS white_losses,
             SUM(CASE WHEN g.win_color IS NULL THEN 1 ELSE 0 END)::int AS draws,
             MAX(g.create_date) AS last_update
         FROM finished_games g
         GROUP BY g.black_id

         UNION ALL

         -- white 관점 집계
         SELECT
             g.white_id AS user_id,
             0::int AS black_wins,
             0::int AS black_losses,
             SUM(CASE WHEN g.win_color = 1 THEN 1 ELSE 0 END)::int AS white_wins,
             SUM(CASE WHEN g.win_color = 0 THEN 1 ELSE 0 END)::int AS white_losses,
             SUM(CASE WHEN g.win_color IS NULL THEN 1 ELSE 0 END)::int AS draws,
             MAX(g.create_date) AS last_update
         FROM finished_games g
         GROUP BY g.white_id
     )
SELECT
    up.user_id,
    COALESCE(SUM(p.black_wins), 0)::int   AS black_wins,
    COALESCE(SUM(p.black_losses), 0)::int AS black_losses,
    COALESCE(SUM(p.white_wins), 0)::int   AS white_wins,
    COALESCE(SUM(p.white_losses), 0)::int AS white_losses,
    COALESCE(SUM(p.draws), 0)::int        AS draws,
    COALESCE(MAX(p.last_update), up.register_date) AS last_update
FROM user_profile up
         LEFT JOIN per_user p ON p.user_id = up.user_id
GROUP BY up.user_id, up.register_date;