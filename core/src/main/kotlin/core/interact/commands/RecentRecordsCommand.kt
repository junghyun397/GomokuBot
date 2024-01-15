package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.assets.aiUser
import core.database.repositories.GameRecordRepository
import core.database.repositories.UserProfileRepository
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.entities.GuildConfig
import utils.lang.tuple
import utils.structs.map

class RecentRecordsCommand(val edit: Boolean) : Command {

        override val name = "records"

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
                    if (record.blackId == user.id) record.whiteId?.let { UserProfileRepository.retrieveUser(bot.dbConnection, it) } ?: aiUser
                    else record.blackId?.let { UserProfileRepository.retrieveUser(bot.dbConnection, it) } ?: aiUser

                tuple(opponent, record)
            }

            val io = service.buildRecentRecords(publisher, config.language.container, user, gameResults)
                .launch()
                .map { emptyOrders }

            tuple(io, this.writeCommandReport("total ${gameRecords.size} records", guild, user))
        }

}
