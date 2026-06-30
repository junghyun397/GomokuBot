package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.interact.i18n.Language
import core.interact.message.PlatformMessage
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.message.SettingMapping
import core.interact.reports.writeCommandReport
import core.session.SessionManager
import core.session.entities.ChannelConfig
import utils.Identifiable
import utils.tuple

class ApplySettingCommand(
    private val newConfig: ChannelConfig,
    private val diff: Identifiable,
) : Command {

    override val name = "apply-setting"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: PlatformService,
        publishers: PublisherSet
    ) = runCatching {
        SessionManager.updateChannelConfig(bot.sessions, channel, this.newConfig)

        val (localKind, localChoice) = SettingMapping.buildKindNamePair(config.language.container, this.diff)

        val io = effect {
            service.buildMessage(
                publishers.windowed,
                PlatformMessage(config.language.container.settingApplied(service.formatHighlight(localKind), service.formatHighlight(localChoice)))
            )
                .launch()()
            Unit
        }

        val (kind, choice) = SettingMapping.buildKindNamePair(Language.ENG.container, this.diff)

        tuple(io, this.writeCommandReport("update $kind as [$choice](${this.diff.id})", channel, user))
    }

}
