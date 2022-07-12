package core.database.repositories

import core.assets.GuildUid
import core.database.DatabaseConnection
import core.interact.i18n.Language
import core.session.ArchivePolicy
import core.session.BoardStyle
import core.session.FocusPolicy
import core.session.SweepPolicy
import core.session.entities.GuildConfig
import kotlinx.coroutines.reactive.awaitSingle
import utils.structs.Option
import utils.structs.find

object GuildConfigRepository {

    suspend fun fetchGuildConfig(connection: DatabaseConnection, guildUid: GuildUid): Option<GuildConfig> =
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement(
                    "SELECT * FROM guild_config WHERE guild_id = $1"
                )
                .bind("$1", guildUid.uuid)
                .execute()
            }
            .flatMap<Option<GuildConfig>> { result -> result
                .map { row, _ ->
                    Option(GuildConfig(
                        language = Language.values().find(row["language"] as Short),
                        boardStyle = BoardStyle.values().find(row["board_style"] as Short),
                        focusPolicy = FocusPolicy.values().find(row["focus_policy"] as Short),
                        sweepPolicy = SweepPolicy.values().find(row["sweep_policy"] as Short),
                        archivePolicy = ArchivePolicy.values().find(row["archive_policy"] as Short)
                    ))
                }
            }
            .defaultIfEmpty(Option.Empty)
            .awaitSingle()

    suspend fun upsertGuildConfig(connection: DatabaseConnection, guildUid: GuildUid, guildConfig: GuildConfig) {
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement(
                    """
                        INSERT INTO guild_config (guild_id, language, board_style, focus_policy, sweep_policy, archive_policy) VALUES ($1, $2, $3, $4, $5, $6)
                            ON CONFLICT (guild_id) DO UPDATE SET language = $2, board_style = $3, focus_policy = $4, sweep_policy = $5, archive_policy = $6
                    """.trimIndent()
                )
                .bind("$1", guildUid.uuid)
                .bind("$2", guildConfig.language.id)
                .bind("$3", guildConfig.boardStyle.id)
                .bind("$4", guildConfig.focusPolicy.id)
                .bind("$5", guildConfig.sweepPolicy.id)
                .bind("$6", guildConfig.archivePolicy.id)
                .execute()
            }
            .flatMap { it.rowsUpdated }
            .awaitSingle()
    }

}
