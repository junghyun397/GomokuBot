package core.database

import core.database.repositories.AnnounceRepository
import io.r2dbc.spi.ConnectionFactories
import jrenju.notation.Flag
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitLast

object DatabaseManager {

    suspend fun newConnectionFrom(url: String, localCaches: LocalCaches): DatabaseConnection =
        DatabaseConnection(
            ConnectionFactories.get(url)
                .create()
                .awaitFirst(),
            localCaches
        )

    suspend fun initDatabase(connection: DatabaseConnection) {
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement(
                    """
                        CREATE TABLE IF NOT EXISTS announce (
                            announce_id SERIAL PRIMARY KEY,
                            contents text NOT NULL,
                            create_date timestamp without time zone DEFAULT now()
                        );
                        
                        CREATE TABLE IF NOT EXISTS guild_profile (
                            guild_id uuid PRIMARY KEY,
                            platform smallint NOT NULL,
                            given_id bigint NOT NULL,
                            name varchar NOT NULL,
                            register_date timestamp without time zone DEFAULT now(),
                            UNIQUE (given_id, platform)
                        );
                        
                        CREATE TABLE IF NOT EXISTS guild_config (
                            guild_id uuid PRIMARY KEY,
                            language smallint NOT NULL,
                            board_style smallint NOT NULL,
                            focus_policy smallint NOT NULL,
                            sweep_policy smallint NOT NULL,
                            archive_policy smallint NOT NULL,
                            FOREIGN KEY (guild_id) REFERENCES guild_profile (guild_id)
                        );
                        
                        CREATE TABLE IF NOT EXISTS user_profile (
                            user_id uuid PRIMARY KEY,
                            platform smallint NOT NULL,
                            given_id bigint NOT NULL,
                            name varchar NOT NULL,
                            name_tag varchar NOT NULL,
                            profile_url varchar NOT NULL,
                            announce_id int,
                            register_date timestamp without time zone DEFAULT now(),
                            FOREIGN KEY (announce_id) REFERENCES announce (announce_id),
                            UNIQUE (given_id, platform)
                        );
                        
                        CREATE TABLE IF NOT EXISTS user_stats (
                            user_id uuid PRIMARY KEY,
                            black_wins int NOT NULL DEFAULT 0,
                            black_losses int NOT NULL DEFAULT 0,
                            black_draws int NOT NULL DEFAULT 0,
                            white_wins int NOT NULL DEFAULT 0,
                            white_losses int NOT NULL DEFAULT 0,
                            white_draws int NOT NULL DEFAULT 0,
                            last_update timestamp without time zone DEFAULT now(),
                            FOREIGN KEY (user_id) REFERENCES user_profile (user_id)
                        );
                            
                        CREATE TABLE IF NOT EXISTS game_record (
                            record_id SERIAL PRIMARY KEY,
                            board_status bytea NOT NULL,
                            history int[] NOT NULL,
                            cause smallint NOT NULL,
                            win_color smallint,
                            guild_id uuid NOT NULL,
                            black_id uuid,
                            white_id uuid,
                            ai_level smallint,
                            create_date timestamp without time zone DEFAULT now(),
                            FOREIGN KEY (guild_id) REFERENCES guild_profile (guild_id),
                            FOREIGN KEY (black_id) REFERENCES user_profile (user_id),
                            FOREIGN KEY (white_id) REFERENCES user_profile (user_id)
                        );
                        
                        CREATE OR REPLACE PROCEDURE upload_game_record(
                            p_board_status bytea,
                            p_history int[],
                            p_cause smallint,
                            p_win_color smallint,
                            p_guild_id uuid,
                            p_black_id uuid,
                            p_white_id uuid,
                            p_ai_level smallint
                        ) LANGUAGE plpgsql AS $$
                        BEGIN
                        
                        INSERT INTO game_record (board_status, history, cause, win_color, guild_id, black_id, white_id, ai_level)
                            VALUES (p_board_status, p_history, p_cause, p_win_color, p_guild_id, p_black_id, p_white_id, p_ai_level);
                            
                        IF p_ai_level IS NOT NULL THEN
                        
                            IF p_black_id IS NOT NULL THEN
                                INSERT INTO user_stats (user_id) VALUES (p_black_id) ON CONFLICT (user_id) DO NOTHING;
                                IF p_win_color = ${Flag.BLACK()} THEN
                                    UPDATE user_stats SET black_wins = black_wins + 1 WHERE user_id = p_black_id;
                                ELSIF p_win_color = ${Flag.WHITE()} THEN
                                    UPDATE user_stats SET black_losses = black_losses + 1 WHERE user_id = p_black_id;
                                ELSE
                                    UPDATE user_stats SET black_draws = black_draws + 1 WHERE user_id = p_black_id;
                                END IF;
                            END IF;
                            
                            IF p_white_id IS NOT NULL THEN
                                INSERT INTO user_stats (user_id) VALUES (p_white_id) ON CONFLICT (user_id) DO NOTHING;
                                IF p_win_color = ${Flag.WHITE()} THEN
                                    UPDATE user_stats SET white_wins = white_wins + 1 WHERE user_id = p_white_id;
                                ELSIF p_win_color = ${Flag.BLACK()} THEN
                                    UPDATE user_stats SET white_losses = white_losses + 1 WHERE user_id = p_white_id;
                                ELSE
                                    UPDATE user_stats SET white_draws = white_draws + 1 WHERE user_id = p_white_id;
                                END IF;
                            END IF;
                         
                        END IF;
                           
                        END $$;
                    """.trimIndent()
                )
                .execute()
            }
            .awaitLast()
    }

    suspend fun initCaches(connection: DatabaseConnection) {
        AnnounceRepository.updateAnnounceCache(connection)
    }

}
