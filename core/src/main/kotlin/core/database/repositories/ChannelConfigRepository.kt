package core.database.repositories

import core.assets.ChannelUid
import core.database.DatabaseConnection
import core.database.jooq.tables.records.ChannelConfigRecord
import core.database.jooq.tables.references.CHANNEL_CONFIG
import core.interact.i18n.Language
import core.interact.message.graphics.HistoryRenderType
import core.session.entities.*
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import reactor.core.publisher.Mono
import utils.find

object ChannelConfigRepository {

    suspend fun retrieveChannelConfig(connection: DatabaseConnection, channelUid: ChannelUid): ChannelConfig {
        connection.localCaches.channelConfigCache
            .getIfPresent(channelUid)
            ?.let { return it }

        val config = this.fetchChannelConfig(connection, channelUid)
            ?: ChannelConfig()

        connection.localCaches.channelConfigCache.put(channelUid, config)

        return config
    }

    suspend fun fetchChannelConfig(connection: DatabaseConnection, channelUid: ChannelUid): ChannelConfig? =
        Mono.from(
            connection.jooq
                .selectFrom(CHANNEL_CONFIG)
                .where(CHANNEL_CONFIG.CHANNEL_ID.eq(channelUid.uuid))
        )
            .map { this.extractChannelConfig(it) }
            .awaitSingleOrNull()

    suspend fun upsertChannelConfig(connection: DatabaseConnection, channelUid: ChannelUid, channelConfig: ChannelConfig) {
        connection.localCaches.channelConfigCache.put(channelUid, channelConfig)

        Mono.from(
            connection.jooq
                .insertInto(CHANNEL_CONFIG)
                .set(CHANNEL_CONFIG.CHANNEL_ID, channelUid.uuid)
                .set(CHANNEL_CONFIG.LANGUAGE, channelConfig.language.id)
                .set(CHANNEL_CONFIG.BOARD_STYLE, channelConfig.boardStyle.id)
                .set(CHANNEL_CONFIG.FOCUS_TYPE, channelConfig.focusType.id)
                .set(CHANNEL_CONFIG.HINT_TYPE, channelConfig.hintType.id)
                .set(CHANNEL_CONFIG.MARK_TYPE, channelConfig.markType.id)
                .set(CHANNEL_CONFIG.SWAP_TYPE, channelConfig.swapType.id)
                .set(CHANNEL_CONFIG.ARCHIVE_POLICY, channelConfig.archivePolicy.id)
                .onConflict(CHANNEL_CONFIG.CHANNEL_ID)
                .doUpdate()
                .set(CHANNEL_CONFIG.LANGUAGE, channelConfig.language.id)
                .set(CHANNEL_CONFIG.BOARD_STYLE, channelConfig.boardStyle.id)
                .set(CHANNEL_CONFIG.FOCUS_TYPE, channelConfig.focusType.id)
                .set(CHANNEL_CONFIG.HINT_TYPE, channelConfig.hintType.id)
                .set(CHANNEL_CONFIG.MARK_TYPE, channelConfig.markType.id)
                .set(CHANNEL_CONFIG.SWAP_TYPE, channelConfig.swapType.id)
                .set(CHANNEL_CONFIG.ARCHIVE_POLICY, channelConfig.archivePolicy.id)
        )
            .awaitSingle()
    }

    private fun extractChannelConfig(record: ChannelConfigRecord): ChannelConfig =
        ChannelConfig(
            language = Language.entries.find(record.language!!),
            boardStyle = BoardStyle.entries.find(record.boardStyle!!),
            focusType = FocusType.entries.find(record.focusType!!),
            hintType = HintType.entries.find(record.hintType!!),
            markType = HistoryRenderType.entries.find(record.markType!!),
            swapType = SwapType.entries.find(record.swapType!!),
            archivePolicy = ArchivePolicy.entries.find(record.archivePolicy!!),
        )

}
