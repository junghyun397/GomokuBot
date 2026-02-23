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
import discord.assets.JDAChannel
import discord.assets.getChannelMessageSubChannelById
import discord.interact.DiscordConfig
import discord.interact.TaskContext
import discord.interact.message.DiscordMessagingService
import discord.interact.message.MessageCreateAdaptor
import discord.interact.message.MessageEditAdaptor
import discord.route.executeIO
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
    jdaChannel: JDAChannel,
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
            executeIO(discordConfig, io, jdaChannel)
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
        SessionManager.cleanExpiredGameSession(bot.sessions).forEach { (_, channelSession, _, session) ->
            val context = TaskContext(bot, channelSession.guild, channelSession.config, LinuxTime.now(), "SCH")

            val maybeMessage = SessionManager.viewHeadMessage(bot.sessions, session.messageBufferKey)

            val maybeChannel = jda.getGuildById(channelSession.guild.givenId.idLong)
            val maybeSubChannel = maybeMessage?.let { maybeChannel?.getChannelMessageSubChannelById(it.subChannelId.idLong) }

            val command = ExpireGameCommand(
                channelSession = channelSession,
                session = session,
                channelAvailable = maybeChannel != null && maybeSubChannel != null
            )

            val result = executeCommand(context, bot, discordConfig, command, maybeSubChannel!!, maybeChannel!!)

            emit(result)
        }

        SessionManager.cleanEmptySessions(bot.sessions)
    }).asFlux()

fun scheduleRequestExpiration(bot: BotContext, discordConfig: DiscordConfig, jda: JDA): Flux<Report> =
    schedule(bot.config.requestExpireCycle, {
        SessionManager.cleanExpiredRequestSessions(bot.sessions).forEach { (_, channelSession, _, session) ->
            val context = TaskContext(bot, channelSession.guild, channelSession.config, LinuxTime.now(), "SCH")

            val maybeMessage = SessionManager.viewHeadMessage(bot.sessions, session.messageBufferKey)

            val maybeChannel = jda.getGuildById(channelSession.guild.givenId.idLong)
            val maybeSubChannel = maybeMessage?.let { maybeChannel?.getChannelMessageSubChannelById(it.subChannelId.idLong) }

            val command = ExpireRequestCommand(
                channelSession = channelSession,
                session = session,
                channelAvailable = maybeChannel != null && maybeSubChannel != null,
                messageAvailable = maybeMessage != null
            )

            val result = executeCommand(context, bot, discordConfig, command, maybeSubChannel!!, maybeChannel!!)

            emit(result)
        }
    }).asFlux()

inline fun routine(interval: Duration, crossinline job: suspend () -> String): Flux<RoutineReport> =
    schedule<RoutineReport>(interval, {
        val time = LinuxTime.now()

        val comment = job()

        RoutineReport(comment, time, LinuxTime.now(), LinuxTime.now(),"SCH")
    }).asFlux()
