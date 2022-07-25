package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.User
import core.database.repositories.AnnounceRepository
import core.database.repositories.UserProfileRepository
import core.interact.i18n.Language
import core.interact.message.MessageAdaptor
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.session.entities.GuildConfig
import kotlinx.coroutines.Deferred
import utils.structs.flatMap

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
    ) = this.command
        .execute(bot, config, guild, user, producer, message, publisher, editPublisher)
        .map { (originalIO, originalReport) ->
            val thenUser = user.copy(announceId = AnnounceRepository.getLatestAnnounceId(bot.dbConnection))

            UserProfileRepository.upsertUser(bot.dbConnection, thenUser)

            val io = AnnounceRepository.getAnnouncesSince(bot.dbConnection, user.announceId ?: -1)
                .map { announces ->
                    producer.produceAnnounce(
                        publisher,
                        config.language.container,
                        announces[config.language] ?: announces[Language.ENG]!!
                    ).flatMap { it.launch() }
                }
                .reduce { acc, io -> acc.flatMap { io } }
                .flatMap { originalIO }

            val report = originalReport.copy(commandName = "$name${originalReport.commandName}")

            io to report
        }

}
