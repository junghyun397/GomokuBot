package discord

import core.BotConfig
import core.BotContext
import core.interact.commands.ExpireGameCommand
import core.interact.commands.ExpireRequestCommand
import core.interact.commands.InternalCommand
import core.interact.message.MonoPublisherSet
import core.interact.reports.ErrorReport
import core.interact.reports.Report
import core.interact.reports.RoutineReport
import core.session.MessageManager
import core.session.SessionManager
import discord.assets.JDAChannel
import discord.assets.getChannelMessageSubChannelById
import discord.interact.DiscordConfig
import discord.interact.TaskContext
import discord.interact.message.DiscordMessagingService
import discord.interact.message.MessageCreateAdaptor
import discord.interact.message.MessageEditAdaptor
import discord.interact.message.asDiscordMessageData
import discord.route.executeIO
import kotlinx.coroutines.flow.Flow
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import utils.schedule
import kotlin.time.Clock
import kotlin.time.Duration

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
        channel = taskContext.channel,
        service = DiscordMessagingService,
        publisher = MonoPublisherSet(
            publisher = { msg -> MessageCreateAdaptor(channel.sendMessage(msg.asDiscordMessageData().buildCreate())) },
            editGlobal = { ref -> { msg -> MessageEditAdaptor(channel.editMessageById(ref.id.idLong, msg.asDiscordMessageData().buildEdit())) } }
        )
    ).fold(
        onSuccess = { (io, report) ->
            executeIO(discordConfig, io, jdaChannel)
            report
        },
        onFailure = { throwable ->
            ErrorReport(throwable, taskContext.channel)
        }
    ).apply {
        interactionSource = taskContext.source
        emittedTime = taskContext.emittedTime
        apiTime = Clock.System.now()
    }

fun scheduleGameExpiration(bot: BotContext, discordConfig: DiscordConfig, jda: JDA): Flow<Report> =
    schedule(BotConfig.gameExpireChecks, {
        SessionManager.cleanExpiredGameSession(bot.sessions).forEach { (_, channel, _, session) ->
            val config = SessionManager.retrieveChannelConfig(bot.sessions, channel)
            val context = TaskContext(bot, channel, config, Clock.System.now(), "SCH")

            val message = MessageManager.viewHeadMessage(bot.sessions, session.messageBufferKey)

            val channel = jda.getGuildById(channel.givenId.idLong)
            val subChannel = message?.let { channel?.getChannelMessageSubChannelById(it.subChannelId.idLong) }

            val command = ExpireGameCommand(
                session = session,
                channelAvailable = channel != null && subChannel != null
            )

            val result = executeCommand(context, bot, discordConfig, command, subChannel!!, channel!!)

            emit(result)
        }
    })

fun scheduleRequestExpiration(bot: BotContext, discordConfig: DiscordConfig, jda: JDA): Flow<Report> =
    schedule(BotConfig.requestExpireChecks, {
        SessionManager.cleanExpiredRequestSessions(bot.sessions).forEach { (_, channel, _, session) ->
            val config = SessionManager.retrieveChannelConfig(bot.sessions, channel)
            val context = TaskContext(bot, channel, config, Clock.System.now(), "SCH")

            val message = MessageManager.viewHeadMessage(bot.sessions, session.messageBufferKey)

            val channel = jda.getGuildById(channel.givenId.idLong)
            val subChannel = message?.let { channel?.getChannelMessageSubChannelById(it.subChannelId.idLong) }

            val command = ExpireRequestCommand(
                session = session,
                channelAvailable = channel != null && subChannel != null,
                messageAvailable = message != null
            )

            val result = executeCommand(context, bot, discordConfig, command, subChannel!!, channel!!)

            emit(result)
        }
    })

inline fun routine(interval: Duration, crossinline job: suspend () -> String): Flow<RoutineReport> =
    schedule(interval, {
        val time = Clock.System.now()

        val comment = job()

        RoutineReport(comment, time, Clock.System.now(), Clock.System.now(),"SCH")
    })
