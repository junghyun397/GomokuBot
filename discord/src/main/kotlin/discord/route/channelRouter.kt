package discord.route

import arrow.core.getOrElse
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
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import reactor.core.publisher.Mono
import utils.assets.LinuxTime
import java.util.*

private fun matchLocale(locale: DiscordLocale): Language =
    when (locale) {
        DiscordLocale.KOREAN -> Language.KOR
        DiscordLocale.JAPANESE -> Language.JPN
        DiscordLocale.VIETNAMESE -> Language.VNM
        else -> Language.ENG
    }

fun channelJoinRouter(context: InternalInteractionContext<GuildJoinEvent>): Mono<Report> =
    mono {
        val guild = ChannelProfileRepository.retrieveOrInsertChannel(context.bot.dbConnection, DISCORD_PLATFORM_ID, context.event.guild.extractId()) {
            Channel(
                id = ChannelUid(UUID.randomUUID()),
                platform = DISCORD_PLATFORM_ID,
                givenId = context.event.guild.extractId(),
                name = context.event.guild.name,
            )
        }

        val config = ChannelConfigRepository.fetchChannelConfig(context.bot.dbConnection, guild.id)
            .getOrElse { ChannelConfig(language = matchLocale(context.event.guild.locale)) }

        val command = ChannelJoinCommand(context.event.guild.locale.languageName)

        command.execute(
            bot = context.bot,
            config = config,
            guild = guild,
            service = DiscordMessagingService,
            publisher = MonoPublisherSet(
                publisher = { msg -> MessageCreateAdaptor(context.event.guild.systemChannel!!.sendMessage(msg.buildCreate()))},
                editGlobal = { throw IllegalStateException() }
            )
        ).fold(
            onSuccess = { (io, report) ->
                executeIO(context.discordConfig, io, context.jdaChannel)
                report
            },
            onFailure = { throwable ->
                ErrorReport(throwable, context.guild)
            }
        ).apply {
            interactionSource = context.source
            emittedTime = context.emittedTime
            apiTime = LinuxTime.now()
        }
    }

// TODO: bad smell
fun channelLeaveRouter(context: InternalInteractionContext<GuildLeaveEvent>): Mono<Report> =
    mono {
        val maybeChannel = ChannelProfileRepository.retrieveChannel(context.bot.dbConnection, DISCORD_PLATFORM_ID, context.event.guild.extractId())

        maybeChannel.fold(
            ifSome = { guild ->
                ChannelLeaveCommand.execute(
                    bot = context.bot,
                    config = SessionManager.retrieveChannelConfig(context.bot.sessions, guild),
                    guild = guild,
                    service = DiscordMessagingService,
                    publisher = MonoPublisherSet(
                        publisher = { throw IllegalStateException() },
                        editGlobal = { throw IllegalStateException() }
                    )
                ).fold(
                    onSuccess = { (_, report) -> report.apply {
                        interactionSource = context.source
                        emittedTime = context.emittedTime
                        apiTime = LinuxTime.now()
                    } },
                    onFailure = { ErrorReport(IllegalStateException(), context.guild) }
                )
            },
            ifEmpty = { ErrorReport(IllegalStateException(), context.guild) }
        )
    }
