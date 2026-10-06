package core.interact.commands

import arrow.core.raise.effect
import core.BotConfig
import core.assets.Channel
import core.assets.User
import core.database.DatabaseConnection
import core.database.repositories.AnnounceRepository
import core.database.repositories.UserProfileRepository
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
import utils.tuple
import kotlin.time.Clock
import kotlin.time.Instant

class AnnounceCommand(command: Command) : UnionCommand(command) {

    override val name = "announce"

    context(dbConnection: DatabaseConnection, mintakaServer: MintakaServer, sessions: SessionPool, service: PlatformService)
    override suspend fun executeSelf(
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        val thenUser = user.copy(announceId = AnnounceRepository.getLatestAnnounceId())

        UserProfileRepository.upsertUser(thenUser)

        val io = effect {
            AnnounceRepository.getAnnouncesSince(user.announceId ?: 0)
                .forEachIndexed { index, announces ->
                    val content = announcementMessage(
                        config.language.container,
                        announces[config.language] ?: announces[Language.ENG]!!,
                    )
                    val message = publishers.plain(content).retrieve()()

                    if (message != null) {
                        NavigationManager.addNavigation(
                            message.ref,
                            PageNavigationState(
                                NavigationKind.ANNOUNCE,
                                index + 1,
                                Clock.System.now() + BotConfig.navigatorExpireAfter
                            )
                        )

                        service.attachBinaryNavigators(message)()
                    }
                }
        }

        val report = this.writeActionLog(emittedTime, "sent", channel, thenUser)

        tuple(io, report, channel, user)
    }

}
