package discord.route

import core.assets.Guild
import core.assets.GuildUid
import core.database.repositories.GuildConfigRepository
import core.database.repositories.GuildProfileRepository
import core.interact.commands.GuildJoinCommand
import core.interact.commands.GuildLeaveCommand
import core.interact.i18n.Language
import core.interact.message.MonoPublisherSet
import core.interact.reports.ErrorReport
import core.interact.reports.Report
import core.session.SessionManager
import core.session.entities.GuildConfig
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
import utils.structs.fold
import utils.structs.orElseGet
import java.util.*

private fun matchLocale(locale: DiscordLocale): Language =
    when (locale) {
        DiscordLocale.KOREAN -> Language.KOR
        DiscordLocale.JAPANESE -> Language.JPN
        DiscordLocale.VIETNAMESE -> Language.VNM
        else -> Language.ENG
    }

fun guildJoinRouter(context: InternalInteractionContext<GuildJoinEvent>): Mono<Report> =
    mono {
        val guild = GuildProfileRepository.retrieveOrInsertGuild(context.bot.dbConnection, DISCORD_PLATFORM_ID, context.event.guild.extractId()) {
            Guild(
                id = GuildUid(UUID.randomUUID()),
                platform = DISCORD_PLATFORM_ID,
                givenId = context.event.guild.extractId(),
                name = context.event.guild.name,
            )
        }

        val config = GuildConfigRepository.fetchGuildConfig(context.bot.dbConnection, guild.id)
            .orElseGet { GuildConfig(language = matchLocale(context.event.guild.locale)) }

        val command = GuildJoinCommand(context.event.guild.locale.languageName)

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
                export(context.discordConfig, io, context.jdaGuild)
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
fun guildLeaveRouter(context: InternalInteractionContext<GuildLeaveEvent>): Mono<Report> =
    mono {
        val maybeGuild = GuildProfileRepository.retrieveGuild(context.bot.dbConnection, DISCORD_PLATFORM_ID, context.event.guild.extractId())

        maybeGuild.fold(
            onDefined = { guild ->
                GuildLeaveCommand.execute(
                    bot = context.bot,
                    config = SessionManager.retrieveGuildConfig(context.bot.sessions, guild),
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
            onEmpty = { ErrorReport(IllegalStateException(), context.guild) }
        )
    }
