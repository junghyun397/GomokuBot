package discord

import core.BotContext
import core.interact.commands.ExpireGameCommand
import core.interact.commands.ExpireRequestCommand
import core.interact.commands.InternalCommand
import core.interact.message.MonoPublisherSet
import core.interact.reports.ErrorReport
import core.interact.reports.Report
import core.interact.reports.RoutineReport
import core.session.SessionManager
import discord.assets.JDAGuild
import discord.interact.DiscordConfig
import discord.interact.TaskContext
import discord.interact.message.DiscordMessagingService
import discord.interact.message.MessageCreateAdaptor
import discord.interact.message.MessageEditAdaptor
import discord.route.export
import kotlinx.coroutines.reactor.asFlux
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import reactor.core.publisher.Flux
import utils.assets.LinuxTime
import utils.lang.schedule
import java.time.Duration

private suspend fun executeCommand(
    taskContext: TaskContext,
    botContext: BotContext,
    discordConfig: DiscordConfig,
    command: InternalCommand,
    channel: MessageChannel,
    jdaGuild: JDAGuild,
): Report =
    command.execute(
        bot = botContext,
        config = taskContext.config,
        guild = taskContext.guild,
        service = DiscordMessagingService,
        publisher = MonoPublisherSet(
            publisher = { msg -> MessageCreateAdaptor(channel.sendMessage(msg.buildCreate())) },
            editGlobal = { ref -> { msg -> MessageEditAdaptor(channel.editMessageById(ref.id.idLong, msg.buildEdit())) } }
        )
    ).fold(
        onSuccess = { (io, report) ->
            export(discordConfig, io, taskContext.guild, jdaGuild)
            report
        },
        onFailure = { throwable ->
            ErrorReport(throwable, taskContext.guild)
        }
    ).apply {
        interactionSource = taskContext.source
        emittedTime = taskContext.emittedTime
        apiTime = LinuxTime.now()
    }

fun scheduleGameExpiration(bot: BotContext, discordConfig: DiscordConfig, jda: JDA): Flux<Report> =
    schedule(bot.config.gameExpireCycle, {
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

            val result = executeCommand(context, bot, discordConfig, command, maybeChannel!!, maybeGuild!!)

            emit(result)
        }

        SessionManager.cleanEmptySessions(bot.sessions)
    }).asFlux()

fun scheduleRequestExpiration(bot: BotContext, discordConfig: DiscordConfig, jda: JDA): Flux<Report> =
    schedule(bot.config.requestExpireCycle, {
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

            val result = executeCommand(context, bot, discordConfig, command, maybeChannel!!, maybeGuild!!)

            emit(result)
        }
    }).asFlux()

inline fun routine(interval: Duration, crossinline job: suspend () -> String): Flux<RoutineReport> =
    schedule<RoutineReport>(interval, {
        val time = LinuxTime.now()

        val comment = job()

        RoutineReport(comment, time, LinuxTime.now(), "SCH")
    }).asFlux()
