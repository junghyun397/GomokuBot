package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.database.repositories.AnnounceRepository
import core.database.repositories.UserProfileRepository
import core.interact.emptyOrders
import core.interact.i18n.Language
import core.interact.message.MessageProducer
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.entities.GuildConfig
import utils.lang.tuple
import utils.structs.flatMap
import utils.structs.map

class AnnounceCommand(command: Command) : UnionCommand(command) {

    override val name = "announce"

    override val responseFlag = this.command.responseFlag

    override suspend fun <A, B> executeSelf(
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
            .map { emptyOrders }

        val report = this.writeCommandReport("succeed", guild, thenUser)

        tuple(io, report, guild, user)
    }

}
