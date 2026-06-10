package discord.route

import core.assets.Channel
import core.assets.ChannelUid
import core.database.repositories.ChannelConfigRepository
import core.database.repositories.ChannelProfileRepository
import core.interact.commands.ChannelJoinCommand
import core.interact.commands.ChannelLeaveCommand
import core.interact.i18n.Language
import core.interact.message.MonoPublisherSet
import core.interact.reports.ErrorReport
import core.interact.reports.Report
import core.session.SessionManager
import core.session.entities.ChannelConfig
import discord.assets.DISCORD_PLATFORM_ID
import discord.assets.extractId
import discord.interact.InternalInteractionContext
import discord.interact.message.DiscordMessagingService
import discord.interact.message.MessageCreateAdaptor
import discord.interact.message.asDiscordMessageData
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.util.*
import kotlin.time.Clock

private fun matchLocale(locale: DiscordLocale): Language =
    when (locale) {
        DiscordLocale.KOREAN -> Language.KOR
        DiscordLocale.JAPANESE -> Language.JPN
        DiscordLocale.VIETNAMESE -> Language.VNM
        else -> Language.ENG
    }

suspend fun channelJoinRouter(context: InternalInteractionContext<GuildJoinEvent>): Report {
    val channel = ChannelProfileRepository.retrieveOrInsertChannel(context.bot.dbConnection, DISCORD_PLATFORM_ID, context.event.guild.extractId()) {
        Channel(
            id = ChannelUid(UUID.randomUUID()),
            platform = DISCORD_PLATFORM_ID,
            givenId = context.event.guild.extractId(),
            name = context.event.guild.name,
        )
    }

    val config = ChannelConfigRepository.fetchChannelConfig(context.bot.dbConnection, channel.id)
        ?: ChannelConfig(language = matchLocale(context.event.guild.locale))

    val command = ChannelJoinCommand(context.event.guild.locale.languageName)

    return command.execute(
        bot = context.bot,
        config = config,
        channel = channel,
        service = DiscordMessagingService,
        publisher = MonoPublisherSet(
            publisher = { msg -> MessageCreateAdaptor(context.event.guild.systemChannel!!.sendMessage(msg.asDiscordMessageData().buildCreate()))},
            editGlobal = { throw IllegalStateException() }
        )
    ).fold(
        onSuccess = { (io, report) ->
            executeIO(context.discordConfig, io, context.jdaChannel)
            report
        },
        onFailure = { throwable ->
            ErrorReport(throwable, context.channel)
        }
    ).apply {
        interactionSource = context.source
        emittedTime = context.emittedTime
        apiTime = Clock.System.now()
    }
}

// TODO: bad smell
suspend fun channelLeaveRouter(context: InternalInteractionContext<GuildLeaveEvent>): Report {
    val channel = ChannelProfileRepository.retrieveChannel(context.bot.dbConnection, DISCORD_PLATFORM_ID, context.event.guild.extractId())

    return (channel?.let {
        ChannelLeaveCommand.execute(
            bot = context.bot,
            config = SessionManager.retrieveChannelConfig(context.bot.sessions, it),
            channel = it,
            service = DiscordMessagingService,
            publisher = MonoPublisherSet(
                publisher = { throw IllegalStateException() },
                editGlobal = { throw IllegalStateException() }
            )
        ).fold(
            onSuccess = { (_, report) -> report },
            onFailure = { ErrorReport(IllegalStateException(), context.channel) }
        )
    } ?: ErrorReport(IllegalStateException(), context.channel)).apply {
        interactionSource = context.source
        emittedTime = context.emittedTime
        apiTime = Clock.System.now()
    }
}
