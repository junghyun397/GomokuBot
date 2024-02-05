package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.assets.retrieveUserOrAiUser
import core.database.repositories.GameRecordRepository
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.entities.GuildConfig
import utils.lang.tuple
import utils.structs.map

class ReplayListCommand(val edit: Boolean) : Command {

        override val name = "replay-list"

        override val responseFlag =
            if (edit) ResponseFlag.DeferEdit
            else ResponseFlag.Immediately

        override suspend fun <A, B> execute(
            bot: BotContext,
            config: GuildConfig,
            guild: Guild,
            user: User,
            service: MessagingService<A, B>,
            messageRef: MessageRef,
            publishers: PublisherSet<A, B>,
        ) = runCatching {
            val gameRecords = GameRecordRepository.retrieveGameRecordsByUserUid(bot.dbConnection, user.id, 10)

            val publisher =
                if (edit) publishers.edit(messageRef)
                else publishers.plain

            val gameResults = gameRecords.map { record ->
                val opponent =
                    if (record.blackId == user.id) record.whiteId.retrieveUserOrAiUser(bot.dbConnection)
                    else record.blackId.retrieveUserOrAiUser(bot.dbConnection)

                tuple(opponent, record)
            }

            val io = service.buildReplayList(publisher, config.language.container, user, gameResults)
                .launch()
                .map { emptyOrders }

            tuple(io, this.writeCommandReport("total ${gameRecords.size} records", guild, user))
        }

}
