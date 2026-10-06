package core.interact.commands

import arrow.core.raise.effect
import core.assets.Channel
import core.assets.User
import core.database.DatabaseConnection
import core.database.repositories.ChannelProfileRepository
import core.engine.MintakaServer
import core.interact.i18n.Language
import core.interact.message.AppMessage
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.SessionManager
import core.session.SessionPool
import core.session.entities.ChannelConfig
import kotlin.time.Instant

class LangCommand(private val language: Language) : Command {

    override val name = "lang"

    override val responseFlag = ResponseFlag.Immediately

    context(dbConnection: DatabaseConnection, mintakaServer: MintakaServer, sessions: SessionPool, service: PlatformService)
    override suspend fun execute(
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        val thenConfig = config.copy(language = this.language)

        SessionManager.updateChannelConfig(channel, thenConfig)

        val io = effect {
            publishers.plain(AppMessage.Text(this@LangCommand.language.container.languageUpdated))
                .launch().bind()

            buildHelpProcedure(thenConfig, publishers.plain, 0).bind()

            service.upsertCommands(thenConfig.language.container)

            ChannelProfileRepository.upsertChannel(
                channel.copy(commandRevision = Command.COMMAND_REVISION)
            )
        }

        CommandResult(io, this.writeActionLog(emittedTime, "${config.language.name} to ${thenConfig.language.name}",
            channel, user))
    }

}
