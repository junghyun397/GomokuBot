package discord

import core.BotContext
import core.database.entities.extractGameRecord
import core.database.repositories.AnnounceRepository
import core.database.repositories.GameRecordRepository
import core.interact.Order
import core.interact.commands.buildFinishSequence
import core.interact.reports.CommandReport
import core.interact.reports.InteractionReport
import core.session.GameManager
import core.session.GameResult
import core.session.SessionManager
import core.session.entities.AiGameSession
import core.session.entities.PvpGameSession
import dev.minn.jda.ktx.coroutines.await
import discord.interact.DiscordConfig
import discord.interact.message.DiscordMessageProducer
import discord.interact.message.DiscordMessagePublisher
import discord.interact.message.MessageActionAdaptor
import discord.route.export
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.reactor.asFlux
import net.dv8tion.jda.api.JDA
import reactor.core.publisher.Flux
import utils.assets.LinuxTime
import utils.lang.schedule
import utils.structs.*

fun scheduleRoutines(botContext: BotContext, discordConfig: DiscordConfig, jda: JDA): Flux<InteractionReport> {
    val expireSessionFlow = schedule<InteractionReport>(botContext.config.gameExpireCycle) {
        SessionManager.cleanExpiredGameSession(botContext.sessions).forEach { (_, guildSession, _, session) ->
            val emittedTime = LinuxTime()

            val (finishedSession, result) = GameManager.resignSession(session, GameResult.Cause.TIMEOUT, session.player)

            finishedSession.extractGameRecord(guildSession.guild.id).forEach { record ->
                GameRecordRepository.uploadGameRecord(botContext.dbConnection, record)
            }

            val message = SessionManager.viewTailMessage(botContext.sessions, session.messageBufferKey)

            val guild = jda.getGuildById(guildSession.guild.givenId.idLong)
            val channel = message?.let { jda.getTextChannelById(it.channelId.idLong) }

            if (message != null && guild != null && channel != null) {
                val publisher: DiscordMessagePublisher = { msg -> MessageActionAdaptor(channel.sendMessage(msg)) }

                val io = when (session) {
                    is PvpGameSession -> DiscordMessageProducer.produceTimeoutPVP(
                        publisher,
                        guildSession.config.language.container,
                        session.player,
                        session.nextPlayer
                    )
                    is AiGameSession -> DiscordMessageProducer.produceTimeoutPVE(
                        publisher,
                        guildSession.config.language.container,
                        session.owner
                    )
                }
                    .launch()
                    .flatMap { buildFinishSequence(botContext, DiscordMessageProducer, publisher, guildSession.config, session, finishedSession) }
                    .map { it + Order.ArchiveSession(finishedSession, guildSession.config.archivePolicy) }

                export(botContext, discordConfig, guild, io, message)
            }

            val report = CommandReport("expire-game", "expired, terminate session by $result", guildSession.guild, session.owner, "SCH", emittedTime)

            emit(report)
        }
    }

    val expireRequestSessionFlow = schedule<InteractionReport>(botContext.config.requestExpireCycle) {
        SessionManager.cleanExpiredRequestSessions(botContext.sessions).forEach { (_, guildSession, _, session) ->
            val emittedTime = LinuxTime()

            val message = SessionManager.viewTailMessage(botContext.sessions, session.messageBufferKey)

            val guild = jda.getGuildById(guildSession.guild.givenId.idLong)
            val channel = message?.let { jda.getTextChannelById(it.channelId.idLong) }

            if (message != null && guild != null && channel != null) {
                val maybeRequestMessage = channel.retrieveMessageById(message.id.idLong)
                    .mapToResult()
                    .map { when {
                        it.isSuccess -> Option(it.get())
                        it.isFailure -> Option.Empty
                        else -> throw IllegalStateException()
                    } }
                    .await()

                val editIO = maybeRequestMessage.fold(
                    onDefined = {
                        val editPublisher: DiscordMessagePublisher = { msg -> MessageActionAdaptor(channel.editMessageById(message.id.idLong, msg)) }

                        DiscordMessageProducer.produceRequestInvalidated(
                            editPublisher,
                            guildSession.config.language.container,
                            session.owner,
                            session.opponent,
                        ).launch()
                    },
                    onEmpty = { IO { } }
                )

                val publisher: DiscordMessagePublisher = maybeRequestMessage.fold(
                    onDefined = { { msg -> MessageActionAdaptor(it.reply(msg)) } },
                    onEmpty = { { msg -> MessageActionAdaptor(channel.sendMessage(msg)) } }
                )

                val noticeIO = DiscordMessageProducer.produceRequestExpired(
                    publisher,
                    guildSession.config.language.container,
                    session.owner,
                    session.opponent,
                ).launch()

                val io = editIO
                    .flatMap { noticeIO }
                    .map { emptyList<Order>() }

                export(botContext, discordConfig, guild, io, null)
            }

            val report = CommandReport("expire-request", "expired, $session rejected", guildSession.guild, session.owner, "SCH", emittedTime)

            emit(report)
        }
    }

    val expireNavigateFlow = schedule<InteractionReport>(botContext.config.navigateExpireCycle) {
        SessionManager.cleanExpiredNavigators(botContext.sessions).forEach { (message, _) ->
            val guild = jda.getGuildById(message.guildId.idLong)
            val channel = guild?.getTextChannelById(message.channelId.idLong)

            if (guild != null && channel != null) {
                val io = IO { listOf(Order.RemoveNavigators(message)) }

                export(botContext, discordConfig, guild, io, message)
            }
        }
    }

    val announceFlow = schedule<InteractionReport>(botContext.config.announceUpdateCycle) {
        AnnounceRepository.updateAnnounceCache(botContext.dbConnection)
    }

    return merge(expireSessionFlow, expireRequestSessionFlow, expireNavigateFlow, announceFlow).asFlux()
}
