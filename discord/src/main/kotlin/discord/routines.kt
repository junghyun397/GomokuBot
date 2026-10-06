package discord

import core.BotConfig
import core.database.DatabaseConnection
import core.interact.commands.ExpireGameCommand
import core.interact.commands.ExpireRequestCommand
import core.interact.commands.InternalCommand
import core.interact.message.MonoPublisherSet
import core.interact.reports.RoutineActionLog
import core.session.SessionManager
import core.session.SessionPool
import discord.assets.JDAChannel
import discord.assets.subChannelById
import discord.interact.DiscordConfig
import discord.interact.TaskContext
import discord.interact.message.DiscordPlatformService
import discord.interact.message.MessageCreateAdaptor
import discord.interact.message.MessageEditAdaptor
import discord.interact.message.discordPublisher
import kotlinx.coroutines.flow.Flow
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.sharding.ShardManager
import utils.schedule
import kotlin.time.Clock
import kotlin.time.Duration

context(dbConnection: DatabaseConnection, sessions: SessionPool)
private suspend fun executeCommand(
    taskContext: TaskContext,
    shardManager: ShardManager,
    discordConfig: DiscordConfig,
    command: InternalCommand,
    jdaChannel: JDAChannel?,
    channel: MessageChannel?,
): List<ActionLogRecord> {
    val platform = DiscordPlatformService(shardManager, discordConfig, jdaChannel)
    val result = context(platform) {
        command.execute(
            config = taskContext.config,
            channel = taskContext.channel,
            publisher = channel?.let { MonoPublisherSet(
                publisher = discordPublisher { msg -> MessageCreateAdaptor(channel.sendMessage(msg.buildCreate())) },
                editGlobal = { ref -> discordPublisher { msg -> MessageEditAdaptor(channel.editMessageById(ref.id.idLong, msg.buildEdit())) } }
            ) },
            emittedTime = taskContext.emittedTime,
        )
    }

    return executeAndRecord(taskContext, result)
}

context(dbConnection: DatabaseConnection, sessions: SessionPool)
fun scheduleGameExpiration(discordConfig: DiscordConfig, shardManager: ShardManager): Flow<ActionLogRecord> =
    schedule(BotConfig.gameExpireChecks, {
        SessionManager.cleanExpiredGameSession().forEach { (_, channel, _, session) ->
            val config = SessionManager.retrieveChannelConfig(channel)
            val context = TaskContext(channel, config, Clock.System.now(), "SCH")

            val message = session.messageRef

            val channel = shardManager.getGuildById(channel.givenId.idLong)
            val subChannel = message?.let { channel?.subChannelById(it.subChannelId.idLong) }

            val command = ExpireGameCommand(session)

            val results = executeCommand(context, shardManager, discordConfig, command, channel, subChannel)

            results.forEach { result -> emit(result) }
        }
    })

context(dbConnection: DatabaseConnection, sessions: SessionPool)
fun scheduleRequestExpiration(discordConfig: DiscordConfig, shardManager: ShardManager): Flow<ActionLogRecord> =
    schedule(BotConfig.requestExpireChecks, {
        SessionManager.cleanExpiredRequestSessions().forEach { (_, channel, _, session) ->
            val config = SessionManager.retrieveChannelConfig(channel)
            val context = TaskContext(channel, config, Clock.System.now(), "SCH")

            val message = session.messageRef

            val channel = shardManager.getGuildById(channel.givenId.idLong)
            val subChannel = message?.let { channel?.subChannelById(it.subChannelId.idLong) }

            val command = ExpireRequestCommand(session)

            val results = executeCommand(context, shardManager, discordConfig, command, channel, subChannel)

            results.forEach { result -> emit(result) }
        }
    })

inline fun routine(interval: Duration, crossinline job: suspend () -> String): Flow<ActionLogRecord> =
    schedule(interval, {
        val startedTime = Clock.System.now()
        val comment = job()
        val executionTime = Clock.System.now() - startedTime

        emit(ActionLogRecord("SCH", Duration.ZERO, RoutineActionLog(comment, executionTime)))
    })
