package core.interact.commands

import core.BotContext
import core.assets.Order
import core.assets.User
import core.interact.message.MessageBinder
import core.interact.message.MessagePublisher
import core.interact.message.SpotInfo
import core.interact.reports.asCommandReport
import core.session.entities.GuildConfig
import utils.monads.Either
import utils.monads.IO

enum class DebugType {
    BOARD_DEMO, STATUS
}

class DebugCommand(override val command: String, private val debugType: DebugType, private val payload: String?) : Command {

    override suspend fun <A, B> execute(
        context: BotContext,
        config: GuildConfig,
        user: User,
        binder: MessageBinder<A, B>,
        publisher: MessagePublisher<A, B>
    ) = runCatching { when (debugType) {
        DebugType.BOARD_DEMO -> {
            val board = Either.Left("BOARDDD")
            val commandMap = arrayOf(
                arrayOf(SpotInfo.FREE, SpotInfo.WHITE, SpotInfo.WHITE, SpotInfo.FREE, SpotInfo.BLACK),
                arrayOf(SpotInfo.FREE, SpotInfo.FREE, SpotInfo.BLACK_RECENT, SpotInfo.BLACK, SpotInfo.WHITE),
                arrayOf(SpotInfo.FREE, SpotInfo.FREE, SpotInfo.WHITE, SpotInfo.WHITE, SpotInfo.WHITE),
                arrayOf(SpotInfo.FREE, SpotInfo.BLACK, SpotInfo.BLACK, SpotInfo.BLACK, SpotInfo.WHITE),
                arrayOf(SpotInfo.FREE, SpotInfo.FREE, SpotInfo.FREE, SpotInfo.WHITE, SpotInfo.BLACK),
            ).mapIndexed { rowIdx, rowS ->
                rowS.mapIndexed { colIdx, el ->
                    "${(colIdx+97).toChar()}${rowIdx+1}" to el
                }.toTypedArray()
            }.toTypedArray()

            val io = binder.bindBoard(publisher, config.language.container, board)
                .map { binder.bindButtons(it.first(), config.language.container, commandMap) }
                .map { it.launch() }
                .map { Order.UNIT }

            io to this.asCommandReport("succeed")
        }
        DebugType.STATUS -> {
            IO { Order.UNIT } to this.asCommandReport("succeed")
        }
    } }

}