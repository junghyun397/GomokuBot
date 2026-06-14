package core.interact.commands

import arrow.core.raise.effect
import core.BotConfig
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.database.repositories.AnnounceRepository
import core.interact.Order
import core.interact.i18n.Language
import core.interact.message.MessagingService
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
        service: MessagingService,
        publishers: PublisherSet,
    ) = runCatching {
        val latestAnnounceId = AnnounceRepository.getLatestAnnounceId(bot.dbConnection)!!
        val announcements = AnnounceRepository.getLatestAnnounce(bot.dbConnection)

        val io = effect {
            service.buildAnnounce(
                publishers.plain,
                language.container,
                announcements[language] ?: announcements[Language.ENG]!!
            ).retrieve()()
                ?.let { announceMessage ->
                        MessageManager.addNavigation(
                            bot.sessions,
                            announceMessage.ref,
                            PageNavigationState(
                                announceMessage.ref,
                                NavigationKind.ANNOUNCE,
                                latestAnnounceId,
                                Clock.System.now() + BotConfig.navigatorExpireAfter
                            )
                        )

                        service.attachBinaryNavigators(announceMessage)()
                    }

            emptyList<Order>()
        }

        tuple(io, this.writeCommandReport("sent", channel, user))
    }

}
