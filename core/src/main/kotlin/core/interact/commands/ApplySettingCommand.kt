package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.emptyOrders
import core.interact.i18n.Language
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.message.SettingMapping
import core.interact.reports.writeCommandReport
import core.session.SessionManager
import core.session.entities.GuildConfig
import utils.lang.tuple
import utils.structs.Identifiable
import utils.structs.map

class ApplySettingCommand(
    private val newConfig: GuildConfig,
    private val diff: Identifiable,
) : Command {

    override val name = "apply-setting"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>
    ) = runCatching {
        SessionManager.updateGuildConfig(bot.sessions, guild, newConfig)

        val (localKind, localChoice) = SettingMapping.buildKindNamePair(config.language.container, this.diff)

        val io = service.buildSettingApplied(publishers.windowed, config.language.container, localKind, localChoice)
            .launch()
            .map { emptyOrders }

        val (kind, choice) = SettingMapping.buildKindNamePair(Language.ENG.container, this.diff)

        tuple(io, this.writeCommandReport("update $kind as [$choice](${diff.id})", guild, user))
    }

}
