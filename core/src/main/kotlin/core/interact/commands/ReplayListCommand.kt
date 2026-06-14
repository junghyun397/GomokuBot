package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.database.repositories.GameRecordRepository
import core.database.repositories.UserProfileRepository
import core.interact.emptyOrders
import core.interact.message.MessagingService
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
        service: MessagingService,
        publishers: PublisherSet,
    ) = runCatching {
        val gameRecords = GameRecordRepository.retrieveGameRecordsByUserUid(bot.dbConnection, user.id, 10)

        val publisher =
            if (this.messageRef != null) publishers.edit(this.messageRef)
            else publishers.plain

        val gameResults = gameRecords.map { record ->
            val opponent = UserProfileRepository.retrieveUser(
                bot.dbConnection,
                record.userUid[!record.userUid.color(user.id)!!]!!
            )

            tuple(opponent, record)
        }

        val io = effect {
            service.buildReplayList(publisher, config.language.container, user, gameResults)
                .launch()()

            emptyOrders
        }

        tuple(io, this.writeCommandReport("total ${gameRecords.size} records", channel, user))
    }

}
