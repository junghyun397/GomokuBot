package core.interact.commands

import arrow.core.raise.effect
import core.BotConfig
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.database.repositories.AnnounceRepository
import core.database.repositories.UserProfileRepository
import core.interact.i18n.Language
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.MessageManager
import core.session.entities.ChannelConfig
import core.session.entities.NavigationKind
import core.session.entities.PageNavigationState
import utils.tuple
import kotlin.time.Clock
import kotlin.time.Instant

class AnnounceCommand(command: Command) : UnionCommand(command) {

    override val name = "announce"

    override suspend fun executeSelf(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: PlatformService,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        val thenUser = user.copy(announceId = AnnounceRepository.getLatestAnnounceId(bot.dbConnection))

        UserProfileRepository.upsertUser(bot.dbConnection, thenUser)

        val io = effect {
            AnnounceRepository.getAnnouncesSince(bot.dbConnection, user.announceId ?: 0)
                .forEachIndexed { index, announces ->
                    val message = service.buildAnnounce(
                        publishers.plain,
                        config.language.container,
                        announces[config.language] ?: announces[Language.ENG]!!
                    ).retrieve()()

                    if (message != null) {
                        MessageManager.addNavigation(
                            bot.sessions,
                            message.ref,
                            PageNavigationState(
                                message.ref,
                                NavigationKind.ANNOUNCE,
                                index + 1,
                                Clock.System.now() + BotConfig.navigatorExpireAfter
                            )
                        )

                        service.attachBinaryNavigators(message)()
                    }
                }

            service.upsertCommands(config.language.container)
        }

        val report = this.writeActionLog(emittedTime, "sent", channel, thenUser)

        tuple(io, report, channel, user)
    }

}
