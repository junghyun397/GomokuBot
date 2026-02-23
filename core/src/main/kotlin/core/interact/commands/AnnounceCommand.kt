package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.database.repositories.AnnounceRepository
import core.database.repositories.UserProfileRepository
import core.interact.Order
import core.interact.i18n.Language
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.SessionManager
import core.session.entities.GuildConfig
import core.session.entities.NavigationKind
import core.session.entities.PageNavigationState
import utils.assets.LinuxTime
import utils.lang.tuple

class AnnounceCommand(command: Command) : UnionCommand(command) {

    override val name = "announce"

    override suspend fun <A, B> executeSelf(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>
    ) = runCatching {
        val thenUser = user.copy(announceId = AnnounceRepository.getLatestAnnounceId(bot.dbConnection))

        UserProfileRepository.upsertUser(bot.dbConnection, thenUser)

        val io = effect {
            AnnounceRepository.getAnnouncesSince(bot.dbConnection, user.announceId ?: 0)
                .forEachIndexed { index, announces ->
                    service.buildAnnounce(
                        publishers.plain,
                        config.language.container,
                        announces[config.language] ?: announces[Language.ENG]!!
                    ).retrieve()()
                        .fold(
                            ifSome = { announceMessage ->
                                SessionManager.addNavigation(
                                    bot.sessions,
                                    announceMessage.messageRef,
                                    PageNavigationState(
                                        announceMessage.messageRef,
                                        NavigationKind.ANNOUNCE,
                                        index + 1,
                                        LinuxTime.nowWithOffset(bot.config.navigatorExpireOffset)
                                    )
                                )

                                service.attachBinaryNavigators(announceMessage.messageData)()
                            },
                            ifEmpty = { }
                        )
                }

            listOf(Order.UpsertCommands(config.language.container))
        }

        val report = this.writeCommandReport("sent", guild, thenUser)

        tuple(io, report, guild, user)
    }

}
