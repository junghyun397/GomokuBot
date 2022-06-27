package discord.route

import core.BotContext
import core.interact.Order
import core.interact.commands.buildFinishSequence
import core.session.GameManager
import core.session.GameResult
import core.session.SessionManager
import core.session.entities.AiGameSession
import core.session.entities.PvpGameSession
import dev.minn.jda.ktx.await
import discord.interact.message.DiscordMessageProducer
import discord.interact.message.DiscordMessagePublisher
import discord.interact.message.MessageActionAdaptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import utils.lang.schedule
import utils.structs.IO
import java.time.Duration

fun scheduleCleaner(logger: org.slf4j.Logger, botContext: BotContext, jda: JDA) {
    val exceptionHandler: (Exception) -> Unit  = { logger.error(it.stackTraceToString()) }

    schedule(Duration.ofHours(1), exceptionHandler) {
        SessionManager.cleanExpiredGameSession(botContext.sessions).forEach { (_, guildSession, _, session) ->
            val message = SessionManager.viewTailMessage(botContext.sessions, session.messageBufferKey)

            val guild = jda.getGuildById(guildSession.guild.givenId.idLong)
            val channel = message?.let { jda.getTextChannelById(it.channelId.idLong) }

            if (message != null && guild != null && channel != null) {
                val publisher: DiscordMessagePublisher = { msg -> MessageActionAdaptor(channel.sendMessage(msg)) }

                val (thenSession, _) = GameManager.resignSession(session, GameResult.Cause.TIMEOUT, session.player)

                val io = when (session) {
                    is PvpGameSession -> DiscordMessageProducer.produceTimeoutPVP(
                        publisher,
                        guildSession.config.language.container,
                        session.nextPlayer,
                        session.player
                    )
                    is AiGameSession -> DiscordMessageProducer.produceTimeoutPVE(
                        publisher,
                        guildSession.config.language.container,
                        session.owner
                    )
                }
                    .map { it.launch() }
                    .flatMap { buildFinishSequence(botContext, DiscordMessageProducer, publisher, guildSession.config, session, thenSession) }

                CoroutineScope(Dispatchers.Default).launch {
                    export(botContext, guild, io, null)
                }
            }
        }

        SessionManager.cleanEmptySessions(botContext.sessions)
    }

    schedule(Duration.ofMinutes(5), exceptionHandler) {
        SessionManager.cleanExpiredRequestSessions(botContext.sessions).forEach { (_, guildSession, _, session) ->
            val message = SessionManager.viewTailMessage(botContext.sessions, session.messageBufferKey)

            val guild = jda.getGuildById(guildSession.guild.givenId.idLong)
            val channel = message?.let { jda.getTextChannelById(it.channelId.idLong) }

            if (message != null && guild != null && channel != null) {
                val publisher: DiscordMessagePublisher = { msg -> MessageActionAdaptor(channel.sendMessage(msg)) }

                val io = DiscordMessageProducer.produceRequestExpired(
                    publisher,
                    guildSession.config.language.container,
                    session.owner,
                    session.opponent
                )
                    .map { it.launch(); Order.DeleteSource }

                CoroutineScope(Dispatchers.Default).launch {
                    try {
                        val original = channel.retrieveMessageById(message.id.idLong).await()
                        export(botContext, guild, io, original)
                    } catch (_: Exception) {}
                }
            }
        }
    }

    schedule(Duration.ofSeconds(5), exceptionHandler) {
        SessionManager.cleanExpiredNavigators(botContext.sessions).forEach { (message, _) ->
            val guild = jda.getGuildById(message.guildId.idLong)
            val channel = guild?.getTextChannelById(message.channelId.idLong)

            if (guild != null && channel != null) {
                val io = IO { Order.RemoveNavigators(message) }

                CoroutineScope(Dispatchers.Default).launch {
                    try {
                        val original = channel.retrieveMessageById(message.id.idLong).await()
                        export(botContext, guild, io, original)
                    } catch (_: Exception) {}
                }
            }
        }
    }
}
