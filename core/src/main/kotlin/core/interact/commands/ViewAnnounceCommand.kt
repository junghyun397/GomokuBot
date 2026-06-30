package core.interact.commands

import arrow.core.raise.effect
import core.BotConfig
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.database.repositories.AnnounceRepository
import core.interact.i18n.Language
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.MessageManager
import core.session.entities.ChannelConfig
import core.session.entities.NavigationKind
import core.session.entities.PageNavigationState
import utils.tuple
import kotlin.time.Clock

class ViewAnnounceCommand(val language: Language) : Command {

    override val name = "view-announce"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: PlatformService,
        publishers: PublisherSet,
    ) = runCatching {
        val latestAnnounceId = AnnounceRepository.getLatestAnnounceId(bot.dbConnection)!!
        val announcements = AnnounceRepository.getLatestAnnounce(bot.dbConnection)

        val io = effect {
            val message = service.buildAnnounce(
                publishers.plain,
                language.container,
                announcements[language] ?: announcements[Language.ENG]!!
            ).retrieve()()

            if (message != null) {
                MessageManager.addNavigation(
                    bot.sessions,
                    message.ref,
                    PageNavigationState(
                        message.ref,
                        NavigationKind.ANNOUNCE,
                        latestAnnounceId,
                        Clock.System.now() + BotConfig.navigatorExpireAfter
                    )
                )

                service.attachBinaryNavigators(message)()
            }
        }

        tuple(io, this.writeCommandReport("sent", channel, user))
    }

}
