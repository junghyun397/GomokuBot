package discord.route

import core.BotContext
import core.interact.Order
import core.interact.commands.attachFinishSequence
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

fun scheduleCleaner(botContext: BotContext, jda: JDA) {
    schedule(Duration.ofHours(1)) {
        SessionManager.cleanExpiredGameSession(botContext.sessionRepository).forEach { combined ->
            val message = SessionManager.viewTailMessage(botContext.sessionRepository, combined.third.messageBufferKey)

            val guild = jda.getGuildById(combined.first.id.idLong)
            val channel = message?.let { jda.getTextChannelById(it.channelId.idLong) }

            if (message != null && guild != null && channel != null) {
                val publisher: DiscordMessagePublisher = { msg -> MessageActionAdaptor(channel.sendMessage(msg)) }

                val thenSession = GameManager.resignSession(combined.third, GameResult.WinCause.TIMEOUT, combined.third.player).first

                val io = when (combined.third) {
                    is PvpGameSession -> DiscordMessageProducer.produceTimeoutPVP(
                        publisher,
                        combined.first.language.container,
                        combined.third.nextPlayer,
                        combined.third.player
                    )
                    is AiGameSession -> DiscordMessageProducer.produceTimeoutPVE(
                        publisher,
                        combined.first.language.container,
                        combined.third.owner
                    )
                }
                    .map { it.launch() }
                    .attachFinishSequence(botContext, DiscordMessageProducer, publisher, combined.first, combined.third, thenSession)

                CoroutineScope(Dispatchers.Default).launch {
                    consumeIO(botContext, guild, io, null)
                }
            }
        }

        SessionManager.cleanEmptySessions(botContext.sessionRepository)
    }

    schedule(Duration.ofMinutes(5)) {
        SessionManager.cleanExpiredRequestSessions(botContext.sessionRepository).forEach { combined ->
            val message = SessionManager.viewTailMessage(botContext.sessionRepository, combined.third.messageBufferKey)

            val guild = jda.getGuildById(combined.first.id.idLong)
            val channel = message?.let { jda.getTextChannelById(it.channelId.idLong) }

            if (message != null && guild != null && channel != null) {
                val publisher: DiscordMessagePublisher = { msg -> MessageActionAdaptor(channel.sendMessage(msg)) }

                val io = DiscordMessageProducer.produceRequestExpired(
                    publisher,
                    combined.first.language.container,
                    combined.third.owner,
                    combined.third.opponent
                )
                    .map { it.launch(); Order.DeleteSource }

                CoroutineScope(Dispatchers.Default).launch {
                    consumeIO(botContext, guild, io, channel.retrieveMessageById(message.id.idLong).await())
                }
            }
        }
    }

    schedule(Duration.ofSeconds(5)) {
        SessionManager.cleanExpiredNavigators(botContext.sessionRepository).forEach { navigate ->
            val guild = jda.getGuildById(navigate.key.guildId.idLong)
            val channel = guild?.getTextChannelById(navigate.key.channelId.idLong)

            if (guild != null && channel != null) {
                val io = IO.unit { Order.RemoveNavigators(navigate.key) }

                CoroutineScope(Dispatchers.Default).launch {
                    consumeIO(botContext, guild, io, channel.retrieveMessageById(navigate.key.id.idLong).await())
                }
            }
        }
    }
}
