package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.inference.AiLevel
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.GameManager
import core.session.Rule
import core.session.SessionManager
import core.session.entities.GuildConfig
import core.session.entities.MessageBufferKey
import core.session.entities.RequestSession
import utils.assets.LinuxTime
import utils.lang.tuple
import utils.structs.IO
import utils.structs.flatMap
import utils.structs.flatMapOption
import utils.structs.map

class StartCommand(val opponent: User?, val rule: Rule) : Command {

    override val name: String = "start"

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
        when(this.opponent) {
            null -> {
                val gameSession = GameManager.generateAiSession(bot, user, AiLevel.AMOEBA)

                SessionManager.putGameSession(bot.sessions, guild, gameSession)

                val io = service.buildBeginsPVE(publishers.plain, config.language.container, user, gameSession.ownerHasBlack)
                    .launch()
                    .flatMap { buildBoardProcedure(bot, guild, config, service, publishers.plain, gameSession) }
                    .map { emptyOrders }

                tuple(io, this.writeCommandReport("start game session with AI", guild, user))
            }
            else -> {
                val requestSession = RequestSession(
                    user, opponent,
                    MessageBufferKey.issue(),
                    this.rule,
                    LinuxTime.nowWithOffset(bot.config.requestExpireOffset),
                )

                SessionManager.putRequestSession(bot.sessions, guild, requestSession)

                val io = service.buildRequest(publishers.plain, config.language.container, user, opponent, this.rule)
                    .retrieve()
                    .flatMapOption { IO { SessionManager.appendMessage(bot.sessions, requestSession.messageBufferKey, it.messageRef) } }
                    .map { emptyOrders }

                tuple(io, this.writeCommandReport("make request to ${this.opponent}", guild, user))
            }
        }
    }

}
