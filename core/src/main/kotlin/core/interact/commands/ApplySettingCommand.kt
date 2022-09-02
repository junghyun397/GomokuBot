package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.Order
import core.interact.message.MessageProducer
import core.interact.message.PublisherSet
import core.interact.reports.asCommandReport
import core.session.SessionManager
import core.session.entities.GuildConfig
import utils.lang.and
import utils.structs.map

class ApplySettingCommand(
    private val newConfig: GuildConfig,
    private val configName: String,
    private val configChoice: String,
) : Command {

    override val name = "apply-setting"

    override val responseFlag = ResponseFlag.IMMEDIATELY

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>
    ) = runCatching {
        SessionManager.updateGuildConfig(bot.sessions, guild, newConfig)

        val io = producer.produceConfigApplied(publishers.windowed, config.language.container, this.configName, this.configChoice)
            .launch()
            .map { emptyList<Order>() }

        io and this.asCommandReport("update $configName as $configChoice", user)
    }

}
