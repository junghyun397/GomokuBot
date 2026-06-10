package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.interact.Order
import core.interact.i18n.Language
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.SessionManager
import core.session.entities.ChannelConfig
import utils.lang.tuple

class LangCommand(private val language: Language) : Command {

    override val name = "lang"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: MessagingService,
        messageRef: MessageRef,
        publishers: PublisherSet,
    ) = runCatching {
        val thenConfig = config.copy(language = this.language)

        SessionManager.updateChannelConfig(bot.sessions, channel, thenConfig)

        val io = effect {
            service.buildLanguageUpdated(publishers.plain, language.container).launch()()
            buildHelpProcedure(bot, thenConfig, publishers.plain, service, 0)()
            listOf(Order.UpsertCommands(thenConfig.language.container))
        }

        tuple(io, this.writeCommandReport("set language ${config.language.name} to ${thenConfig.language.name}",
            channel, user))
    }

}
