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
import discord.interact.message.MessageActionRestActionAdaptor
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.Region
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple2
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

fun guildJoinRouter(context: InteractionContext<GuildJoinEvent>): Mono<Tuple2<InteractionContext<GuildJoinEvent>, Result<GuildJoinReport>>> =
    Mono.zip(context.toMono(), mono { runCatching {
        val commandInserted = run {
            context.event.guild.selfMember.hasPermission(Permission.MESSAGE_SEND)
            GuildManager.insertCommands(context.event.guild, context.config.language.container)
            true
        }

        val defaultRegion = retrieveRegion(context.event.guild)
        val matchedLanguage = matchLanguage(defaultRegion)

        DatabaseManager.updateGuildConfig(
            context.botContext.databaseConnection, context.guild.id,
            GuildConfig(context.guild.id, language = matchedLanguage)
        )

        val helpSent = context.event.guild.systemChannel?.let { channel ->
            val messagePublisher: DiscordMessagePublisher = { msg -> MessageActionRestActionAdaptor(channel.sendMessage(msg)) }

            GuildManager.permissionSafeRun(channel, Permission.MESSAGE_SEND) {
                DiscordMessageProducer.produceAboutBot(messagePublisher, context.config.language.container).map { it.retrieve() }
                    .flatMap { DiscordMessageProducer.produceCommandGuide(messagePublisher, context.config.language.container).map { it.retrieve() } }
                    .flatMap { DiscordMessageProducer.produceStyleGuide(messagePublisher, context.config.language.container).map { it.retrieve() } }
                    .flatMap { DiscordMessageProducer.produceLanguageGuide(messagePublisher).map { it.launch() } }
            }
        }?.isDefined

        GuildJoinReport(commandInserted, helpSent, defaultRegion.name, matchedLanguage)
    } } )
