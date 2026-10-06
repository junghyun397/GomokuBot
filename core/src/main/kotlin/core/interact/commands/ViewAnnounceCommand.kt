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
import core.interact.message.announcementMessage
import core.interact.reports.writeActionLog
import core.session.NavigationManager
import core.session.entities.ChannelConfig
import core.session.entities.NavigationKind
import core.session.entities.PageNavigationState
import kotlin.time.Clock
import kotlin.time.Instant

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
        emittedTime: Instant,
    ) = runCatching {
        val latestAnnounceId = AnnounceRepository.getLatestAnnounceId(bot.dbConnection)!!
        val announcements = AnnounceRepository.getLatestAnnounce(bot.dbConnection)

        val io = effect {
            val language = this@ViewAnnounceCommand.language
            val content = announcementMessage(language.container, announcements[language] ?: announcements[Language.ENG]!!)
            val message = publishers.plain(content).retrieve()()

            if (message != null) {
                NavigationManager.addNavigation(
                    bot.sessions,
                    message.ref,
                    PageNavigationState(
                        NavigationKind.ANNOUNCE,
                        latestAnnounceId,
                        Clock.System.now() + BotConfig.navigatorExpireAfter
                    )
                )

                service.attachBinaryNavigators(message)()
            }
        }

        CommandResult(io, this.writeActionLog(emittedTime, "sent", channel, user))
    }

}
