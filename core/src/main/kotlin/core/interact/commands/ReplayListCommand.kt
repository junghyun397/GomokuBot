package core.interact.commands

import arrow.core.raise.effect
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.database.DatabaseConnection
import core.database.repositories.GameRecordRepository
import core.engine.MintakaServer
import core.interact.message.*
import core.interact.reports.writeActionLog
import core.session.SessionPool
import core.session.entities.ChannelConfig
import kotlin.time.Instant

class ReplayListCommand(
    private val messageRef: MessageRef?,
) : Command {

    override val name = "replay-list"

    override val responseFlag =
        if (this.messageRef == null) ResponseFlag.Immediately
        else ResponseFlag.DeferEdit

    context(dbConnection: DatabaseConnection, mintakaServer: MintakaServer, sessions: SessionPool, service: PlatformService)
    override suspend fun execute(
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        val gameRecords = GameRecordRepository.retrieveGameRecords(user.id, 10)

        if (gameRecords.isEmpty())
            return@runCatching CommandResult(
                effect { },
                this.writeActionLog(emittedTime, "no records", channel, user)
            )

        val publisher =
            if (this.messageRef != null) publishers.edit(this.messageRef)
            else publishers.plain

        val io = effect {
            val view = ReplayListView(config.language.container, user, gameRecords.map { it.buildReplayEntry(user) })
            publisher(AppMessage.ReplayList(view))
                .launch()()
        }

        CommandResult(io, this.writeActionLog(emittedTime, "${gameRecords.size} records", channel, user))
    }

}
