package core.interact.commands

import arrow.core.Option
import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.database.repositories.UserRatingRepository
import core.interact.message.AppMessage
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.EngineGameManager
import core.session.PvpGameManager
import core.session.SessionManager
import core.session.entities.ChannelConfig
import core.session.entities.Rule
import kotlin.time.Instant

class StartCommand(
    val recipient: Option<User.Human>,
    val rule: Rule,
) : Command {

    override val name: String = "start"

    override val responseFlag = ResponseFlag.Defer

    override suspend fun execute(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: PlatformService,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        this.recipient.fold(
            ifSome = { recipient ->
                val requestSession = PvpGameManager.request(user, recipient, this.rule)

                SessionManager.createRequestSession(bot.sessions, channel, setOf(user.id, recipient.id), requestSession)

                val io = SessionManager.retrieveRequestSession(bot.sessions, requestSession.id).interact { runtime ->
                    buildRequestProcedure(config, service, publishers, runtime)
                }

                CommandResult(io, this.writeActionLog(emittedTime, "request to ${this.recipient}", channel, user))
            },
            ifEmpty = {
                val rating = UserRatingRepository.retrieveUserRating(bot.dbConnection, user.id)
                val engineLevel = EngineGameManager.matchEngineLevel(rating)

                val session = EngineGameManager.create(bot.mintakaServer, user, rating, engineLevel)

                SessionManager.insertGameSession(bot.sessions, channel, session)

                val board = SessionManager.retrieveGameSession(bot.sessions, session.id).interact { runtime ->
                    buildBoardProcedure(config, service, publishers, runtime)
                }
                val io = effect {
                    val notice = if (session.users.black == user)
                        config.language.container.beginEngineWhite(service.formatUser(user), service.formatUser(User.GomokuBot))
                    else
                        config.language.container.beginEngineBlack(service.formatUser(user), service.formatUser(User.GomokuBot))

                    publishers.plain(AppMessage.Text(notice)).launch()()
                    board()
                }

                CommandResult(io, this.writeActionLog(emittedTime, "$engineLevel", channel, user))
            }
        )
    }

}
