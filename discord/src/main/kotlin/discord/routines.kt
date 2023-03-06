package discord

import core.BotContext
import core.interact.ExecutionContext
import core.interact.commands.CommandResult
import core.interact.commands.ExpireGameCommand
import core.interact.commands.ExpireRequestCommand
import core.interact.commands.InternalCommand
import core.interact.message.MonoPublisherSet
import core.interact.reports.RoutineReport
import core.session.SessionManager
import core.session.entities.GuildSession
import discord.interact.TaskContext
import discord.interact.message.DiscordMessagingService
import discord.interact.message.MessageCreateAdaptor
import discord.interact.message.MessageEditAdaptor
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.reactor.asFlux
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import reactor.core.publisher.Flux
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import utils.assets.LinuxTime
import utils.lang.schedule
import java.time.Duration

private suspend fun executeCommand(
    command: InternalCommand,
    bot: BotContext,
    guildSession: GuildSession,
    channel: MessageChannel,
): CommandResult =
    command.execute(
        bot = bot,
        config = guildSession.config,
        guild = guildSession.guild,
        service = DiscordMessagingService,
        publisher = MonoPublisherSet(
            publisher = { msg -> MessageCreateAdaptor(channel.sendMessage(msg.buildCreate())) },
            editGlobal = { ref -> { msg -> MessageEditAdaptor(channel.editMessageById(ref.id.idLong, msg.buildEdit())) } }
        )
    )

fun scheduleExpireRoutines(bot: BotContext, jda: JDA): Flux<Tuple2<ExecutionContext, CommandResult>> {
    val expireGameSessionFlow = schedule<Tuple2<ExecutionContext, CommandResult>>(bot.config.gameExpireCycle, {
        SessionManager.cleanExpiredGameSession(bot.sessions).forEach { (_, guildSession, _, session) ->
            val context = TaskContext(bot, guildSession.guild, guildSession.config, LinuxTime.now(), "SCH")

            val maybeMessage = SessionManager.viewHeadMessage(bot.sessions, session.messageBufferKey)

            val maybeGuild = jda.getGuildById(guildSession.guild.givenId.idLong)
            val maybeChannel = maybeMessage?.let { maybeGuild?.getTextChannelById(it.channelId.idLong) }

            val command = ExpireGameCommand(
                guildSession = guildSession,
                session = session,
                channelAvailable = maybeGuild != null && maybeChannel != null
            )

            val result = executeCommand(command, bot, guildSession, maybeChannel!!)

            emit(Tuples.of(context, result))
        }

        SessionManager.cleanEmptySessions(bot.sessions)
    })

    val expireRequestSessionFlow = schedule<Tuple2<ExecutionContext, CommandResult>>(bot.config.requestExpireCycle, {
        SessionManager.cleanExpiredRequestSessions(bot.sessions).forEach { (_, guildSession, _, session) ->
            val context = TaskContext(bot, guildSession.guild, guildSession.config, LinuxTime.now(), "SCH")

            val maybeMessage = SessionManager.viewHeadMessage(bot.sessions, session.messageBufferKey)

            val maybeGuild = jda.getGuildById(guildSession.guild.givenId.idLong)
            val maybeChannel = maybeMessage?.let { jda.getTextChannelById(it.channelId.idLong) }

            val command = ExpireRequestCommand(
                guildSession = guildSession,
                session = session,
                channelAvailable = maybeGuild != null && maybeChannel != null,
                messageAvailable = maybeMessage != null
            )

            val result = executeCommand(command, bot, guildSession, maybeChannel!!)

            emit(Tuples.of(context, result))
        }
    })

    return merge(expireGameSessionFlow, expireRequestSessionFlow).asFlux()
}

inline fun routine(interval: Duration, crossinline job: suspend () -> String): Flux<RoutineReport> =
    schedule<RoutineReport>(interval, {
        val time = LinuxTime.now()

        val comment = job()

        RoutineReport(comment, time, "SCH")
    }).asFlux()
