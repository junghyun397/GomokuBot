package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.database.repositories.AnnounceRepository
import core.database.repositories.UserProfileRepository
import core.interact.Order
import core.interact.i18n.Language
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.MessageManager
import core.session.entities.ChannelConfig
import core.session.entities.NavigationKind
import core.session.entities.PageNavigationState
import utils.lang.tuple
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

class AnnounceCommand(command: Command) : UnionCommand(command) {

    override val name = "announce"

    override suspend fun executeSelf(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: MessagingService,
        messageRef: MessageRef,
        publishers: PublisherSet
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
                        ?.let { announceMessage ->
                                MessageManager.addNavigation(
                                    bot.sessions,
                                    announceMessage.ref,
                                    PageNavigationState(
                                        announceMessage.ref,
                                        NavigationKind.ANNOUNCE,
                                        index + 1,
                                        Clock.System.now() + bot.config.navigatorExpireAfter.milliseconds
                                    )
                                )

                                service.attachBinaryNavigators(announceMessage)()
                            }
                }

            listOf(Order.UpsertCommands(config.language.container))
        }

        val report = this.writeCommandReport("sent", channel, thenUser)

        tuple(io, report, channel, user)
    }

}
