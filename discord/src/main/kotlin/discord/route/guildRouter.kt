package discord.route

import core.database.DatabaseManager
import core.interact.i18n.Language
import core.interact.reports.GuildJoinReport
import core.session.entities.GuildConfig
import discord.assets.JDAGuild
import discord.interact.GuildManager
import discord.interact.InteractionContext
import discord.interact.message.DiscordMessageProducer
import discord.interact.message.DiscordMessagePublisher
import discord.interact.message.MessageActionAdaptor
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.Region
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple2
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private suspend fun retrieveRegion(guild: JDAGuild): Region =
    suspendCoroutine { control ->
        guild.retrieveRegions()
            .map { it.first() }
            .queue { control.resume(it) }
    }

private fun matchLanguage(region: Region): Language =
    when (region) {
        Region.SOUTH_KOREA, Region.VIP_SOUTH_KOREA -> Language.KOR
        Region.JAPAN, Region.VIP_JAPAN -> Language.JPN
        else -> Language.ENG
    }

private fun matchLanguage(locale: Locale): Language =
    when (locale) {
        Locale.KOREA, Locale.KOREAN -> Language.KOR
        Locale.JAPAN, Locale.JAPANESE -> Language.JPN
        else -> Language.ENG
    }

fun guildJoinRouter(context: InteractionContext<GuildJoinEvent>): Mono<Tuple2<InteractionContext<GuildJoinEvent>, Result<GuildJoinReport>>> =
    Mono.zip(context.toMono(), mono { runCatching {
        val defaultLocale = context.event.guild.locale
        val matchedLanguage = matchLanguage(defaultLocale)

        DatabaseManager.updateGuildConfig(
            context.bot.databaseConnection, context.guild.id,
            GuildConfig(context.guild.id, language = matchedLanguage)
        )

        val commandInserted = run {
            GuildManager.upsertCommands(context.event.guild, matchedLanguage.container)
            true
        }

        val helpSent = context.event.guild.systemChannel?.let { channel ->
            val messagePublisher: DiscordMessagePublisher = { msg -> MessageActionAdaptor(channel.sendMessage(msg)) }

            GuildManager.permissionSafeRun(channel, Permission.MESSAGE_SEND) {
                DiscordMessageProducer.produceWelcomeKit(messagePublisher, matchedLanguage.container)
                    .map { it.launch() }
            }
        }?.isDefined

        GuildJoinReport(commandInserted, helpSent, defaultLocale.displayName, matchedLanguage)
    } } )
