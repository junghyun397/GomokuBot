package core.interact.commands

import arrow.core.Either
import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.database.repositories.UserRatingRepository
import core.engine.EngineLevel
import core.interact.message.PlatformMessage
import core.interact.message.PlatformService
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
    val recipient: Either<User.Human, EngineLevel>,
    val rule: Rule,
) : Command {

    override val name: String = "start"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: PlatformService,
        publishers: PublisherSet,
    ) = runCatching {
        this.recipient.fold(
            ifLeft = { recipient ->
                val requestSession = PvpGameManager.request(user, recipient, this.rule)

                SessionManager.createRequestSession(bot.sessions, channel, setOf(user.id, recipient.id), requestSession)

                val io = effect {
                    val message = service.buildRequest(
                        publishers.plain,
                        config.language.container,
                        user, recipient, this@StartCommand.rule
                    )
                        .retrieve()()

                    if (message != null)
                        MessageManager.appendMessage(bot.sessions, requestSession.messageBufferKey, message.ref)
                }

                tuple(io, this.writeCommandReport("make request to ${this.recipient}", channel, user))
            },
            ifRight = { engineLevel ->
                val rating = UserRatingRepository.retrieveUserRating(bot.dbConnection, user.id)

                val session = EngineGameManager.create(bot.mintakaServer, user, rating, engineLevel)

                SessionManager.insertGameSession(bot.sessions, channel, session)

                val io = effect {
                    service.buildMessage(
                        publishers.plain,
                        PlatformMessage(
                            if (session.users.black == user)
                                config.language.container.beginEngineWhite(service.formatUser(user))
                            else
                                config.language.container.beginEngineBlack(service.formatUser(user))
                        )
                    )
                        .launch()()
                    buildBoardProcedure(bot, config, service, publishers.plain, session)()
                }

                tuple(io, this.writeCommandReport("start game session with Engine", channel, user))
            }
        )
    }

}
