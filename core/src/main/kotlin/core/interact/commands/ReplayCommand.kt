package core.interact.commands

import arrow.core.raise.effect
import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.assets.buildBoard
import core.database.entities.GameRecordId
import core.database.entities.asGameSession
import core.database.repositories.GameRecordRepository
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.entities.GuildConfig
import utils.lang.tuple

class ReplayCommand(private val recordId: GameRecordId, private val moves: Int) : Command {

    override val name = "replay"

    override val responseFlag = ResponseFlag.Immediately

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>,
    ) = runCatching {
        val originalRecord = GameRecordRepository.retrieveGameRecordByRecordId(bot.dbConnection, this.recordId).getOrNull()!!

        if (originalRecord.history.isEmpty()) {
            val io = effect {
                service.buildUnableToReplay(publishers.edit(messageRef), config.language.container)
                    .addComponents(service.buildBackToListButton())
                    .launch()()
                emptyOrders
            }

            return@runCatching tuple(io, this.writeCommandReport("${this.recordId} is empty", guild, user))
        }

        val modRecord = run {
            val modifiedHistory = originalRecord.history.subList(0, moves)

            originalRecord.copy(
                history = modifiedHistory,
                boardState = buildBoard(modifiedHistory)
            )
        }

        val session = modRecord.asGameSession(bot.dbConnection, user)

        val io = effect {
            service.buildReplayBoard(
                publishers.edit(messageRef),
                config.language.container,
                config.boardStyle.renderer,
                config.markType,
                session,
                originalRecord.history.size
            )
                .addComponents(service.buildReplayButtons(this@ReplayCommand.recordId, user.id.validationKey, originalRecord.history.size, moves))
                .launch()()

            emptyOrders
        }

        tuple(io, this.writeCommandReport("gameId = ${this.recordId.id}, moves = $moves", guild, user))
    }

}
