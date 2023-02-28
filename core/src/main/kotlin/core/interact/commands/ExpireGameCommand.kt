package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.interact.Order
import core.interact.emptyOrders
import core.interact.message.MessageProducer
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.GameManager
import core.session.GameResult
import core.session.SessionManager
import core.session.SwapType
import core.session.entities.*
import utils.lang.shift
import utils.lang.tuple
import utils.structs.IO
import utils.structs.flatMap
import utils.structs.map

class ExpireGameCommand(
    private val guildSession: GuildSession,
    private val session: GameSession,
    private val channelAvailable: Boolean,
) : InternalCommand {

    override val name = "expire-game"

    private fun writeReport(result: GameResult, guild: Guild) =
        this.writeCommandReport("expired, terminate session by $result", guild)

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        producer: MessageProducer<A, B>,
        publisher: PublisherSet<A, B>,
    ) = runCatching {
        val (finishedSession, result) = GameManager.resignSession(session, GameResult.Cause.TIMEOUT, session.player)

        GameManager.finishSession(bot, guildSession.guild, finishedSession, result)

        val io = if (this.channelAvailable) {
            val message = SessionManager.viewHeadMessage(bot.sessions, session.messageBufferKey)

            val noticePublisher = publisher.plain

            val boardPublisher = noticePublisher
                .shift(guildSession.config.swapType == SwapType.EDIT && message != null) { publisher.edit(message!!) }

            val finishIO = buildFinishProcedure(bot, producer, boardPublisher, guildSession.config, session, finishedSession)
                .map { it + Order.ArchiveSession(finishedSession, guildSession.config.archivePolicy) }

            val noticeIO = when (session) {
                is PvpGameSession -> producer
                    .produceTimeoutPVP(noticePublisher, guildSession.config.language.container, session.player, session.nextPlayer)
                is AiGameSession -> producer
                    .produceTimeoutPVE(noticePublisher, guildSession.config.language.container, session.owner)
            }
                .launch()

            noticeIO.flatMap { finishIO }
        } else IO.value(emptyOrders)

        tuple(io, this.writeReport(result, guild))
    }

}
