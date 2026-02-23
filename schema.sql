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

CREATE VIEW user_stats AS
WITH hvai AS (
    SELECT
        record_id,
        ai_level,
        win_color,
        create_date,
        CASE
            WHEN black_id IS NOT NULL AND white_id IS NULL THEN black_id
            WHEN white_id IS NOT NULL AND black_id IS NULL THEN white_id
            END AS user_id,
        CASE
            WHEN black_id IS NOT NULL AND white_id IS NULL THEN 0
            WHEN white_id IS NOT NULL AND black_id IS NULL THEN 1
            END AS human_color
    FROM game_record
    WHERE ai_level IS NOT NULL
      AND (black_id IS NULL) <> (white_id IS NULL)
)
SELECT
    user_id,

    SUM(CASE WHEN human_color = 0 AND win_color = 0 THEN 1 ELSE 0 END)::INTEGER AS black_wins,
    SUM(CASE WHEN human_color = 0 AND win_color = 1 THEN 1 ELSE 0 END)::INTEGER AS black_losses,

    SUM(CASE WHEN human_color = 1 AND win_color = 1 THEN 1 ELSE 0 END)::INTEGER AS white_wins,
    SUM(CASE WHEN human_color = 1 AND win_color = 0 THEN 1 ELSE 0 END)::INTEGER AS white_losses,

    SUM(CASE WHEN win_color IS NULL THEN 1 ELSE 0 END)::int AS draws,

    MAX(create_date) AS last_update

FROM hvai
WHERE user_id IS NOT NULL
GROUP BY user_id;
