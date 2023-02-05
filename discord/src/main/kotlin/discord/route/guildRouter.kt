package discord.route

import core.BotContext
import core.assets.Guild
import core.assets.GuildUid
import core.database.repositories.GuildConfigRepository
import core.database.repositories.GuildProfileRepository
import core.interact.commands.buildCombinedHelpProcedure
import core.interact.i18n.Language
import core.interact.reports.ErrorReport
import core.interact.reports.InteractionReport
import core.interact.reports.ServerJoinReport
import core.interact.reports.ServerLeaveReport
import core.session.SessionManager
import core.session.entities.GuildConfig
import discord.assets.DISCORD_PLATFORM_ID
import discord.assets.extractId
import discord.assets.extractProfile
import discord.assets.getEventAbbreviation
import discord.interact.GuildManager
import discord.interact.message.DiscordMessageProducer
import discord.interact.message.MessageCreateAdaptor
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.Permission
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

fun guildJoinRouter(bot: BotContext, event: GuildJoinEvent): Mono<ServerJoinReport> =
    mono {
        val emittedTime = LinuxTime.now()

        val guild = GuildProfileRepository.retrieveOrInsertGuild(bot.dbConnection, DISCORD_PLATFORM_ID, event.guild.extractId()) {
            Guild(
                id = GuildUid(UUID.randomUUID()),
                platform = DISCORD_PLATFORM_ID,
                givenId = event.guild.extractId(),
                name = event.guild.name,
            )
        }

        val config = GuildConfigRepository.fetchGuildConfig(bot.dbConnection, guild.id)
            .orElseGet { GuildConfig(language = matchLocale(event.guild.locale)) }

        SessionManager.updateGuildConfig(bot.sessions, guild, config)

        val commandInserted = runCatching {
            GuildManager.upsertCommands(event.guild, config.language.container)
        }.isSuccess

        val helpSent = event.guild.systemChannel?.run {
            GuildManager.permissionGrantedRun(this, Permission.MESSAGE_SEND) {
                buildCombinedHelpProcedure(
                    bot = bot,
                    config = config,
                    publisher = { msg -> MessageCreateAdaptor(this.sendMessage(msg.buildCreate())) },
                    producer = DiscordMessageProducer,
                    settingsPage = 0
                ).run()
            }
        }?.isDefined

        ServerJoinReport(commandInserted, helpSent, event.guild.locale.languageName, config.language, guild, getEventAbbreviation(GuildJoinEvent::class), emittedTime)
    }

fun guildLeaveRouter(bot: BotContext, event: GuildLeaveEvent): Mono<InteractionReport> =
    mono {
        val emittedTime = LinuxTime.now()

        val maybeGuild = GuildProfileRepository.retrieveGuild(bot.dbConnection, DISCORD_PLATFORM_ID, event.guild.extractId())

        maybeGuild.fold(
            onDefined = { guild -> ServerLeaveReport(guild, getEventAbbreviation(GuildLeaveEvent::class), emittedTime) },
            onEmpty = { ErrorReport(IllegalStateException(), event.guild.extractProfile(), getEventAbbreviation(GuildLeaveEvent::class), emittedTime) }
        )
    }
