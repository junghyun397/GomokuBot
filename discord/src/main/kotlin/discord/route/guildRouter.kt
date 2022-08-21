package discord.route

import core.BotContext
import core.assets.Guild
import core.assets.GuildUid
import core.database.repositories.GuildConfigRepository
import core.database.repositories.GuildProfileRepository
import core.interact.commands.buildCombinedHelpSequence
import core.interact.i18n.Language
import core.interact.reports.ServerJoinReport
import core.session.SessionManager
import core.session.entities.GuildConfig
import discord.assets.DISCORD_PLATFORM_ID
import discord.assets.extractId
import discord.interact.GuildManager
import discord.interact.message.DiscordMessageProducer
import discord.interact.message.MessageActionAdaptor
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import reactor.core.publisher.Mono
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
                buildCombinedHelpSequence(
                    bot = bot,
                    config = config,
                    publisher = { msg -> MessageActionAdaptor(this.sendMessage(msg)) },
                    producer = DiscordMessageProducer,
                    settingsPage = 0
                ).run()
            }
        }?.isDefined

        ServerJoinReport(guild, commandInserted, helpSent, event.guild.locale.languageName, config.language)
    }
