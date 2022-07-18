package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.User
import core.database.repositories.AnnounceRepository
import core.database.repositories.UserProfileRepository
import core.interact.Order
import core.interact.i18n.Language
import core.interact.message.MessageAdaptor
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.CommandReport
import core.session.entities.GuildConfig
import kotlinx.coroutines.Deferred
import utils.structs.IO
import utils.structs.flatMap
import utils.structs.map

class AnnounceCommand(override val name: String, private val command: Command) : Command {

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        message: Deferred<MessageAdaptor<A, B>>,
        publisher: MessagePublisher<A, B>,
        editPublisher: MessagePublisher<A, B>
    ) : Result<Pair<IO<List<Order>>, CommandReport>> {
        val thenUser = user.copy(announceId = AnnounceRepository.getLatestAnnounceId(bot.dbConnection))

        UserProfileRepository.upsertUser(bot.dbConnection, thenUser)

        return this.command
            .execute(bot, config, guild, user, producer, message, publisher, editPublisher)
            .map { (originalIO, report) ->
                val io = AnnounceRepository.getAnnouncesSince(bot.dbConnection, user.announceId ?: -1)
                    .map { announces ->
                        producer.produceAnnounce(
                            publisher,
                            config.language.container,
                            announces[config.language] ?: announces[Language.ENG]!!
                        ).map { it.launch() }
                    }
                    .reduce { acc, io -> acc.flatMap { io } }
                    .flatMap { originalIO }

                io to report
            }
    }

}
