package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.assets.retrieveUserOrGomokuBot
import core.database.repositories.GameRecordRepository
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.entities.ChannelConfig
import utils.lang.tuple

class ReplayListCommand(val edit: Boolean) : Command {

        override val name = "replay-list"

        override val responseFlag =
            if (edit) ResponseFlag.DeferEdit
            else ResponseFlag.Immediately

        override suspend fun execute(
            bot: BotContext,
            config: ChannelConfig,
            channel: Channel,
            user: User.Human,
            service: MessagingService,
            messageRef: MessageRef,
            publishers: PublisherSet,
        ) = runCatching {
            val gameRecords = GameRecordRepository.retrieveGameRecordsByUserUid(bot.dbConnection, user.id, 10)

            val publisher =
                if (edit) publishers.edit(messageRef)
                else publishers.plain

            val gameResults = gameRecords.map { record ->
                val opponent =
                    if (record.blackId == user.id) record.whiteId.retrieveUserOrGomokuBot(bot.dbConnection)
                    else record.blackId.retrieveUserOrGomokuBot(bot.dbConnection)

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
