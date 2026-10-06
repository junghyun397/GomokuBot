package core.interact.commands

import arrow.core.Option
import arrow.core.raise.effect
import core.assets.Channel
import core.assets.User
import core.database.DatabaseConnection
import core.database.repositories.UserRatingRepository
import core.engine.MintakaServer
import core.interact.message.AppMessage
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.EngineGameManager
import core.session.PvpGameManager
import core.session.SessionManager
import core.session.SessionPool
import core.session.entities.ChannelConfig
import core.session.entities.Rule
import kotlin.time.Instant

class StartCommand(
    val recipient: Option<User.Human>,
    val rule: Rule,
) : Command {

    override val name: String = "start"

    override val responseFlag = ResponseFlag.Defer

    context(dbConnection: DatabaseConnection, mintakaServer: MintakaServer, sessions: SessionPool, service: PlatformService)
    override suspend fun execute(
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        this.recipient.fold(
            ifSome = { recipient ->
                val requestSession = PvpGameManager.request(user, recipient, this.rule)

                SessionManager.createRequestSession(channel, setOf(user.id, recipient.id), requestSession)

                val io = SessionManager.retrieveRequestSession(requestSession.id).interact { runtime ->
                    buildRequestProcedure(config, publishers, runtime)
                }

                CommandResult(io, this.writeActionLog(emittedTime, "request to ${this.recipient}", channel, user))
            },
            ifEmpty = {
                val rating = UserRatingRepository.retrieveUserRating(user.id)
                val engineLevel = EngineGameManager.matchEngineLevel(rating)

                val session = EngineGameManager.create(user, rating, engineLevel)

                SessionManager.insertGameSession(channel, session)

                val board = SessionManager.retrieveGameSession(session.id).interact { runtime ->
                    buildBoardProcedure(config, publishers, runtime)
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
