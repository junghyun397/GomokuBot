package core.database.repositories

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import core.assets.ChannelUid
import core.database.DatabaseConnection
import core.interact.i18n.Language
import core.interact.message.graphics.HistoryRenderType
import core.session.*
import core.session.entities.ChannelConfig
import kotlinx.coroutines.reactive.awaitSingle
import utils.structs.find

object ChannelConfigRepository {

    suspend fun fetchChannelConfig(connection: DatabaseConnection, channelUid: ChannelUid): Option<ChannelConfig> =
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement(
                    "SELECT * FROM guild_config WHERE guild_id = $1"
                )
                .bind("$1", channelUid.uuid)
                .execute()
            }
            .flatMap<Option<ChannelConfig>> { result -> result
                .map { row, _ ->
                    Some(ChannelConfig(
                        language = Language.entries.find(row["language"] as Short),
                        boardStyle = BoardStyle.entries.find(row["board_style"] as Short),
                        focusType = FocusType.entries.find(row["focus_type"] as Short),
                        hintType = HintType.entries.find(row["hint_type"] as Short),
                        markType = HistoryRenderType.entries.find(row["mark_type"] as Short),
                        swapType = SwapType.entries.find(row["swap_type"] as Short),
                        archivePolicy = ArchivePolicy.entries.find(row["archive_policy"] as Short),
                    ))
                }
            }
            .defaultIfEmpty(None)
            .awaitSingle()

    suspend fun upsertChannelConfig(connection: DatabaseConnection, channelUid: ChannelUid, channelConfig: ChannelConfig) {
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement(
                    """
                        INSERT INTO guild_config (guild_id, language, board_style, focus_type, hint_type, mark_type, swap_type, archive_policy) VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
                            ON CONFLICT (guild_id) DO UPDATE SET language = $2, board_style = $3, focus_type = $4, hint_type = $5, mark_type = $6, swap_type = $7, archive_policy = $8
                    """.trimIndent()
                )
                .bind("$1", channelUid.uuid)
                .bind("$2", channelConfig.language.id)
                .bind("$3", channelConfig.boardStyle.id)
                .bind("$4", channelConfig.focusType.id)
                .bind("$5", channelConfig.hintType.id)
                .bind("$6", channelConfig.markType.id)
                .bind("$7", channelConfig.swapType.id)
                .bind("$8", channelConfig.archivePolicy.id)
                .execute()
            }
            .flatMap { it.rowsUpdated }
            .awaitSingle()
    }

}
