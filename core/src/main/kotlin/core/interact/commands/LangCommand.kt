package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.interact.i18n.Language
import core.interact.message.PlatformMessage
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.SessionManager
import core.session.entities.ChannelConfig
import kotlin.time.Instant

class LangCommand(private val language: Language) : Command {

    override val name = "lang"

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
        val thenConfig = config.copy(language = this.language)

        SessionManager.updateChannelConfig(bot.sessions, channel, thenConfig)

        val io = effect {
            service.buildMessage(publishers.plain, PlatformMessage(this@LangCommand.language.container.languageUpdated()))
                .launch()()
            buildHelpProcedure(bot, thenConfig, publishers.plain, service, 0)()
            service.upsertCommands(thenConfig.language.container)
        }

        CommandResult(io, this.writeActionLog(emittedTime, "${config.language.name} to ${thenConfig.language.name}",
            channel, user))
    }

}
