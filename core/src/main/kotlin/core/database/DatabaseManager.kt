package core.database

import io.r2dbc.spi.ConnectionFactories
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

    suspend fun initTables(connection: DatabaseConnection) {
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement(
                    """
                        CREATE TABLE IF NOT EXISTS public.guild_profile (
                            guild_id uuid PRIMARY KEY,
                            platform smallint NOT NULL,
                            given_id bigint NOT NULL,
                            name varchar NOT NULL,
                            register_date timestamp without time zone DEFAULT now(),
                            UNIQUE (given_id, platform)
                        );
                        
                        CREATE TABLE IF NOT EXISTS public.guild_config (
                            guild_id uuid PRIMARY KEY,
                            language smallint NOT NULL,
                            board_style smallint NOT NULL,
                            focus_policy smallint NOT NULL,
                            sweep_policy smallint NOT NULL,
                            archive_policy smallint NOT NULL,
                            FOREIGN KEY (guild_id) REFERENCES guild_profile (guild_id)
                        );
                        
                        CREATE TABLE IF NOT EXISTS public.user_profile (
                            user_id uuid PRIMARY KEY,
                            platform smallint NOT NULL,
                            given_id bigint NOT NULL,
                            name varchar NOT NULL,
                            name_tag varchar NOT NULL,
                            profile_url varchar NOT NULL,
                            register_date timestamp without time zone DEFAULT now(),
                            UNIQUE (given_id, platform)
                        );
                        
                        CREATE TABLE IF NOT EXISTS public.user_stats (
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
                            
                        CREATE TABLE IF NOT EXISTS public.game_record (
                            record_id SERIAL PRIMARY KEY,
                            board_status bytea NOT NULL,
                            history int[],
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
                            board_status bytea,
                            history int[],
                            cause smallint,
                            win_color smallint,
                            guild_id uuid,
                            black_id uuid,
                            white_id uuid,
                            ai_level smallint
                        ) LANGUAGE sql AS $$
                        
                        BEGIN;
                        
                        INSERT INTO game_record (board_status, history, cause, win_color, guild_id, black_id, white_id, ai_level)
                            VALUES (board_status, history, cause, win_color, guild_id, black_id, white_id, ai_level);
                        
                        END;
                        $$;
                    """.trimIndent()
                )
                .execute()
            }
            .awaitLast()
    }

}
