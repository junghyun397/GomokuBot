package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.database.repositories.UserRatingRepository
import core.engine.EngineLevel
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.EngineGameManager
import core.session.MessageManager
import core.session.PvpGameManager
import core.session.SessionManager
import core.session.entities.ChannelConfig
import core.session.entities.Rule
import utils.tuple

class StartCommand(
    val recipient: User,
    val rule: Rule,
) : Command {

    override val name: String = "start"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: MessagingService,
        publishers: PublisherSet,
    ) = runCatching {
        when (this.recipient) {
            is User.GomokuBot -> {
                val rating = UserRatingRepository.retrieveUserRating(bot.dbConnection, user.id)

                val session = EngineGameManager.create(bot.mintakaServer, user, rating, EngineLevel.MODERATE)

                SessionManager.insertGameSession(bot.sessions, channel, session)

                val io = effect {
                    service.buildBeginsEngine(publishers.plain, config.language.container, user, session.users.black == user)
                        .launch()()
                    buildBoardProcedure(bot, config, service, publishers.plain, session)()
                    emptyOrders
                }

                tuple(io, this.writeCommandReport("start game session with Engine", channel, user))
            }
            is User.Human -> {
                val requestSession = PvpGameManager.request(user, this.recipient, this.rule)

                SessionManager.createRequestSession(bot.sessions, channel, setOf(user.id, recipient.id), requestSession)

                val io = effect {
                    service.buildRequest(publishers.plain, config.language.container, user, recipient, this@StartCommand.rule)
                        .retrieve()()
                        ?.let { MessageManager.appendMessage(bot.sessions, requestSession.messageBufferKey, it.ref) }

                    emptyOrders
                }

                tuple(io, this.writeCommandReport("make request to ${this.recipient}", channel, user))
            }
        }
    }

}
