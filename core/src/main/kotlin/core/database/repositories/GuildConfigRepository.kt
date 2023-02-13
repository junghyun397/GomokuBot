package core.database.repositories

import core.assets.GuildUid
import core.database.DatabaseConnection
import core.interact.i18n.Language
import core.interact.message.graphics.HistoryRenderType
import core.session.*
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
            .flatMap { result -> result
                .map { row, _ ->
                    Option(GuildConfig(
                        language = Language.values().find(row["language"] as Short),
                        boardStyle = BoardStyle.values().find(row["board_style"] as Short),
                        focusPolicy = FocusPolicy.values().find(row["focus_policy"] as Short),
                        hintPolicy = HintPolicy.values().find(row["hint_policy"] as Short),
                        markPolicy = HistoryRenderType.values().find(row["mark_policy"] as Short),
                        swapPolicy = SwapPolicy.values().find(row["swap_policy"] as Short),
                        archivePolicy = ArchivePolicy.values().find(row["archive_policy"] as Short),
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
                        INSERT INTO guild_config (guild_id, language, board_style, focus_policy, hint_policy, mark_policy, swap_policy, archive_policy) VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
                            ON CONFLICT (guild_id) DO UPDATE SET language = $2, board_style = $3, focus_policy = $4, hint_policy = $5, mark_policy = $6, swap_policy = $7, archive_policy = $8
                    """.trimIndent()
                )
                .bind("$1", guildUid.uuid)
                .bind("$2", guildConfig.language.id)
                .bind("$3", guildConfig.boardStyle.id)
                .bind("$4", guildConfig.focusPolicy.id)
                .bind("$5", guildConfig.hintPolicy.id)
                .bind("$6", guildConfig.markPolicy.id)
                .bind("$7", guildConfig.swapPolicy.id)
                .bind("$8", guildConfig.archivePolicy.id)
                .execute()
            }
            .flatMap { it.rowsUpdated }
            .awaitSingle()
    }

}
