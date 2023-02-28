package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.inference.AiLevel
import core.interact.emptyOrders
import core.interact.message.MessageProducer
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.GameManager
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

class StartCommand(val opponent: User?) : Command {

    override val name: String = "start"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>,
    ) = runCatching {
        when(this.opponent) {
            null -> {
                val gameSession = GameManager.generateAiSession(bot, user, AiLevel.AMOEBA)

                SessionManager.putGameSession(bot.sessions, guild, gameSession)

                val io = producer.produceBeginsPVE(publishers.plain, config.language.container, user, gameSession.ownerHasBlack)
                    .launch()
                    .flatMap { buildBoardProcedure(bot, guild, config, producer, publishers.plain, gameSession) }
                    .map { emptyOrders }

                tuple(io, this.writeCommandReport("start game session with AI", guild, user))
            }
            else -> {
                val requestSession = RequestSession(
                    user, opponent,
                    MessageBufferKey.issue(),
                    LinuxTime.nowWithOffset(bot.config.requestExpireOffset),
                )

                SessionManager.putRequestSession(bot.sessions, guild, requestSession)

                val io = producer.produceRequest(publishers.plain, config.language.container, user, opponent)
                    .retrieve()
                    .flatMapOption { IO { SessionManager.appendMessage(bot.sessions, requestSession.messageBufferKey, it.messageRef) } }
                    .map { emptyOrders }

                tuple(io, this.writeCommandReport("make request to ${this.opponent}", guild, user))
            }
        }
    }

}
