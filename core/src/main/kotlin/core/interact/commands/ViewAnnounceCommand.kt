package core.interact.commands

import arrow.core.raise.effect
import core.BotConfig
import core.assets.Channel
import core.assets.User
import core.database.DatabaseConnection
import core.database.repositories.AnnounceRepository
import core.engine.MintakaServer
import core.interact.i18n.Language
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.message.announcementMessage
import core.interact.reports.writeActionLog
import core.session.NavigationManager
import core.session.SessionPool
import core.session.entities.ChannelConfig
import core.session.entities.NavigationKind
import core.session.entities.PageNavigationState
import kotlin.time.Clock
import kotlin.time.Instant

class ViewAnnounceCommand(val language: Language) : Command {

    override val name = "view-announce"

    override val responseFlag = ResponseFlag.Immediately

    context(dbConnection: DatabaseConnection, mintakaServer: MintakaServer, sessions: SessionPool, service: PlatformService)
    override suspend fun execute(
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        val latestAnnounceId = AnnounceRepository.getLatestAnnounceId()!!
        val announcements = AnnounceRepository.getLatestAnnounce()

        val io = effect {
            val language = this@ViewAnnounceCommand.language
            val content = announcementMessage(language.container, announcements[language] ?: announcements[Language.ENG]!!)
            val message = publishers.plain(content).retrieve().bind()

            if (message != null) {
                NavigationManager.addNavigation(
                    message.ref,
                    PageNavigationState(
                        NavigationKind.ANNOUNCE,
                        latestAnnounceId,
                        Clock.System.now() + BotConfig.navigatorExpireAfter
                    )
                )

                service.attachBinaryNavigators(message).bind()
            }
        }

        CommandResult(io, this.writeActionLog(emittedTime, "sent", channel, user))
    }

}
