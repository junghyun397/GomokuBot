package core.interact.commands

import arrow.core.raise.effect
import core.assets.Channel
import core.assets.User
import core.database.DatabaseConnection
import core.engine.MintakaServer
import core.interact.i18n.Language
import core.interact.message.AppMessage
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.message.SettingMapping
import core.interact.reports.writeActionLog
import core.session.SessionManager
import core.session.SessionPool
import core.session.entities.ChannelConfig
import utils.Identifiable
import kotlin.time.Instant

class ApplySettingCommand(
    private val newConfig: ChannelConfig,
    private val diff: Identifiable,
) : Command {

    override val name = "apply-setting"

    override val responseFlag = ResponseFlag.Immediately

    context(dbConnection: DatabaseConnection, mintakaServer: MintakaServer, sessions: SessionPool, service: PlatformService)
    override suspend fun execute(
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        SessionManager.updateChannelConfig(channel, this.newConfig)

        val (localKind, localChoice) = SettingMapping.buildKindNamePair(config.language.container, this.diff)

        val io = effect {
            val notice = config.language.container.settingApplied(service.formatHighlight(localKind), service.formatHighlight(localChoice))
            publishers.windowed(AppMessage.Text(notice))
                .launch()()
        }

        val (kind, choice) = SettingMapping.buildKindNamePair(Language.ENG.container, this.diff)

        CommandResult(io, this.writeActionLog(emittedTime, "$kind as [$choice](${this.diff.id})", channel, user))
    }

}
