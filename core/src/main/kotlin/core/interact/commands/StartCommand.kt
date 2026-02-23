package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
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
import core.session.entities.ChannelConfig
import core.session.entities.MessageBufferKey
import core.session.entities.RequestSession
import utils.assets.LinuxTime
import utils.lang.tuple

class StartCommand(val opponent: User?, val rule: Rule) : Command {

    override val name: String = "start"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: ChannelConfig,
        guild: Channel,
        user: User,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>,
    ) = runCatching {
        when(this.opponent) {
            null -> {
                val gameSession = GameManager.generateAiSession(bot, user, AiLevel.AMOEBA)

                SessionManager.putGameSession(bot.sessions, guild, gameSession)

                val io = effect {
                    service.buildBeginsPVE(publishers.plain, config.language.container, user, gameSession.ownerHasBlack)
                        .launch()()
                    buildBoardProcedure(bot, guild, config, service, publishers.plain, gameSession)()
                    emptyOrders
                }

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

                val io = effect {
                    service.buildRequest(publishers.plain, config.language.container, user, opponent, this@StartCommand.rule)
                        .retrieve()()
                        .fold(
                            ifSome = { SessionManager.appendMessage(bot.sessions, requestSession.messageBufferKey, it.messageRef) },
                            ifEmpty = { }
                        )

                    emptyOrders
                }

                tuple(io, this.writeCommandReport("make request to ${this.opponent}", guild, user))
            }
        }
    }

}
