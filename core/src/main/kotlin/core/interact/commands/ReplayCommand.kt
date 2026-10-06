package core.interact.commands

import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.assets.Channel
import core.assets.MessageRef
import core.assets.User
import core.database.DatabaseConnection
import core.database.entities.GameRecord
import core.engine.MintakaServer
import core.interact.message.AppMessage
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.message.buildBoardDraw
import core.interact.reports.writeActionLog
import core.session.SessionPool
import core.session.entities.ChannelConfig
import renju.Board
import renju.GameState
import kotlin.time.Instant

class ReplayCommand(
    private val record: GameRecord,
    private val messageRef: MessageRef,
) : Command {

    override val name = "replay"

    override val responseFlag = ResponseFlag.Immediately

    context(dbConnection: DatabaseConnection, mintakaServer: MintakaServer, sessions: SessionPool, service: PlatformService)
    override suspend fun execute(
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        val io: Effect<Nothing, Unit> = effect {
            val record = this@ReplayCommand.record
            val state = GameState(Board.fromHistory(record.history), record.history)
            publishers.edit(this@ReplayCommand.messageRef)(AppMessage.Replay(record.buildBoardDraw(state)))
                .launch().bind()
        }

        CommandResult(io, this.writeActionLog(emittedTime, "view record", channel, user))
    }

}
