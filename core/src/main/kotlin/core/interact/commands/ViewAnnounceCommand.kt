package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.database.repositories.AnnounceRepository
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
import utils.structs.flatMapOption
import utils.structs.map

class ViewAnnounceCommand(val language: Language) : Command {

    override val name = "view-announce"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>,
    ) = runCatching {
        val latestAnnounceId = AnnounceRepository.getLatestAnnounceId(bot.dbConnection)!!
        val announcements = AnnounceRepository.getLatestAnnounce(bot.dbConnection)

        val io = service.buildAnnounce(
            publishers.plain,
            language.container,
            announcements[language] ?: announcements[Language.ENG]!!
        ).retrieve()
            .flatMapOption { announceMessage ->
                SessionManager.addNavigation(
                    bot.sessions,
                    announceMessage.messageRef,
                    PageNavigationState(
                        announceMessage.messageRef,
                        NavigationKind.ANNOUNCE,
                        latestAnnounceId,
                        LinuxTime.nowWithOffset(bot.config.navigatorExpireOffset)
                    )
                )

                service.attachBinaryNavigators(announceMessage.messageData)
            }
            .map { emptyList<Order>() }

        tuple(io, this.writeCommandReport("sent", guild, user))
    }

}
