package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.database.repositories.AnnounceRepository
import core.database.repositories.UserProfileRepository
import core.interact.i18n.Language
import core.interact.message.MessageProducer
import core.interact.message.PublisherSet
import core.session.entities.GuildConfig
import utils.lang.pair
import utils.structs.flatMap

class AnnounceCommand(private val command: Command) : Command {

    override val name = "announce+"

    override val responseFlag = this.command.responseFlag

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>
    ) = runCatching {
        val thenUser = user.copy(announceId = AnnounceRepository.getLatestAnnounceId(bot.dbConnection))

        UserProfileRepository.upsertUser(bot.dbConnection, thenUser)

        val io = AnnounceRepository.getAnnouncesSince(bot.dbConnection, user.announceId ?: -1)
            .map { announces ->
                producer.produceAnnounce(
                    publishers.plain,
                    config.language.container,
                    announces[config.language] ?: announces[Language.ENG]!!
                ).launch()
            }
            .reduce { acc, io -> acc.flatMap { io } }

        this.command
            .execute(bot, config, guild, user, producer, messageRef, publishers)
            .map { (originalIO, originalReport) ->
                io.flatMap { originalIO } pair originalReport.copy(commandName = "$name${originalReport.commandName}")
            }
            .getOrThrow()
    }

}
