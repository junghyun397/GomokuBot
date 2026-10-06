package discord.route

import core.assets.Channel
import core.assets.ChannelUid
import core.database.DatabaseConnection
import core.database.repositories.ChannelConfigRepository
import core.database.repositories.ChannelProfileRepository
import core.interact.commands.ChannelJoinCommand
import core.interact.commands.ChannelLeaveCommand
import core.interact.i18n.Language
import core.interact.message.MonoPublisherSet
import core.session.SessionManager
import core.session.SessionPool
import core.session.entities.ChannelConfig
import discord.ActionLogRecord
import discord.asActionLogRecord
import discord.assets.DISCORD_PLATFORM_ID
import discord.assets.channelId
import discord.executeAndRecord
import discord.interact.InternalInteractionContext
import discord.interact.message.DiscordPlatformService
import discord.interact.message.MessageCreateAdaptor
import discord.interact.message.discordPublisher
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.util.*

private fun matchLocale(locale: DiscordLocale): Language =
    when (locale) {
        DiscordLocale.KOREAN -> Language.KOR
        DiscordLocale.JAPANESE -> Language.JPN
        else -> Language.ENG
    }

context(dbConnection: DatabaseConnection, sessions: SessionPool)
suspend fun channelJoinRouter(context: InternalInteractionContext<GuildJoinEvent>): List<ActionLogRecord> {
    val channel = ChannelProfileRepository.retrieveOrInsertChannel(DISCORD_PLATFORM_ID, context.event.guild.channelId()) {
        Channel(
            id = ChannelUid(UUID.randomUUID()),
            platform = DISCORD_PLATFORM_ID,
            givenId = context.event.guild.channelId(),
            name = context.event.guild.name,
        )
    }

    val config = ChannelConfigRepository.fetchChannelConfig(channel.id)
        ?: ChannelConfig(language = matchLocale(context.event.guild.locale))

    val command = ChannelJoinCommand(context.event.guild.locale.languageName)
    val platform = DiscordPlatformService(context.shardManager, context.discordConfig, context.jdaChannel)

    val result = context(platform) {
        command.execute(
            config = config,
            channel = channel,
            publisher = context.event.guild.systemChannel?.let { systemChannel ->
                MonoPublisherSet(
                    publisher = discordPublisher { msg -> MessageCreateAdaptor(systemChannel.sendMessage(msg.buildCreate()))},
                    editGlobal = { throw IllegalStateException() }
                )
            },
            emittedTime = context.emittedTime,
        )
    }

    return executeAndRecord(context, result)
}

context(dbConnection: DatabaseConnection, sessions: SessionPool)
suspend fun channelLeaveRouter(context: InternalInteractionContext<GuildLeaveEvent>): List<ActionLogRecord> {
    val channel = ChannelProfileRepository.retrieveChannel(DISCORD_PLATFORM_ID, context.event.guild.channelId())

    return if (channel != null) {
        val config = SessionManager.retrieveChannelConfig(channel)
        val platform = DiscordPlatformService(context.shardManager, context.discordConfig, context.jdaChannel)
        val result = context(platform) {
            ChannelLeaveCommand.execute(
                config = config,
                channel = channel,
                publisher = MonoPublisherSet(
                    publisher = { throw IllegalStateException() },
                    editGlobal = { throw IllegalStateException() }
                ),
                emittedTime = context.emittedTime,
            )
        }

        executeAndRecord(context, result)
    } else
        IllegalStateException().asActionLogRecord(context)
}
