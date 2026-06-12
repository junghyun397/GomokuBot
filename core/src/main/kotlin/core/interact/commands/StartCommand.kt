package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.mintaka.EngineLevel
import core.session.GameManager
import core.session.MessageManager
import core.session.SessionManager
import core.session.entities.*
import utils.lang.tuple
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

class StartCommand(val opponent: User, val rule: Rule) : Command {

    override val name: String = "start"

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
        when(this.opponent) {
            is User.GomokuBot -> {
                val gameSession = GameManager.generateEngineSession(bot, user, EngineLevel.AMOEBA)

                SessionManager.createGameSession(bot.sessions, channel, gameSession.participantIds, gameSession)

                val io = effect {
                    service.buildBeginsEngine(publishers.plain, config.language.container, user, gameSession.user.black == user)
                        .launch()()
                    buildBoardProcedure(bot, config, service, publishers.plain, gameSession)()
                    emptyOrders
                }

                tuple(io, this.writeCommandReport("start game session with Engine", channel, user))
            }
            is User.Human -> {
                val requestSession = RequestSession(
                    id = SessionId.issue(),
                    requester = user,
                    opponent = opponent,
                    messageBufferKey = MessageBufferKey.issue(),
                    rule = this.rule,
                    expireDate = Clock.System.now() + bot.config.requestExpireAfter.milliseconds,
                )

                SessionManager.createRequestSession(bot.sessions, channel, setOf(user.id, opponent.id), requestSession)

                val io = effect {
                    service.buildRequest(publishers.plain, config.language.container, user, opponent, this@StartCommand.rule)
                        .retrieve()()
                        ?.let { MessageManager.appendMessage(bot.sessions, requestSession.messageBufferKey, it.ref) }

                    emptyOrders
                }

                tuple(io, this.writeCommandReport("make request to ${this.opponent}", channel, user))
            }
            else -> throw IllegalStateException()
        }
    }

}
