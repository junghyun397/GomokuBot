package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.database.repositories.GameRecordRepository
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.entities.ChannelConfig
import utils.tuple

class ReplayListCommand(
    private val messageRef: MessageRef?,
) : Command {

    override val name = "replay-list"

    override val responseFlag =
        if (this.messageRef == null) ResponseFlag.Immediately
        else ResponseFlag.DeferEdit

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: PlatformService,
        publishers: PublisherSet,
    ) = runCatching {
        val gameRecords = GameRecordRepository.retrieveGameRecords(bot.dbConnection, user.id, 10)

        if (gameRecords.isEmpty())
            return@runCatching tuple(
                effect { },
                this.writeCommandReport("no records", channel, user)
            )

        val publisher =
            if (this.messageRef != null) publishers.edit(this.messageRef)
            else publishers.plain

        val io = effect {
            service.buildReplayList(publisher, config.language.container, user, gameRecords)
                .launch()()
        }

        tuple(io, this.writeCommandReport("total ${gameRecords.size} records", channel, user))
    }

}
