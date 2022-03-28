package core.interact.commands

import core.BotContext
import core.interact.message.MessageBinder
import core.interact.message.MessagePublisher
import core.interact.message.SpotInfo
import core.interact.reports.CommandReport
import core.interact.reports.asCommandReport
import core.session.entities.GuildConfig
import utils.monads.Either
import utils.monads.IO
import utils.values.UserId

class HelpCommand(override val command: String) : Command {

    override suspend fun <A, B> execute(
        botContext: BotContext,
        guildConfig: GuildConfig,
        userId: UserId,
        messageBinder: MessageBinder<A, B>,
        messagePublisher: MessagePublisher<A, B>
    ): Result<Pair<IO<Unit>, CommandReport>> = runCatching {
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

        val io = messageBinder.bindAboutBot(messagePublisher, guildConfig.language.container).map { it.launch() }
            .flatMap { messageBinder.bindCommandGuide(messagePublisher, guildConfig.language.container).map { it.launch() } }
            .flatMap {
                messageBinder.bindBoard(messagePublisher, guildConfig.language.container, board)
                    .map { messageBinder.bindButtons(it.first(), guildConfig.language.container, commandMap) }
                    .map { it.launch() }
            }

        io to this.asCommandReport("succeed")
    }

}
