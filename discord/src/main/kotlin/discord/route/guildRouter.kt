package discord.route

import core.interact.commands.buildHelpSequence
import core.interact.i18n.Language
import core.interact.reports.ServerJoinReport
import core.session.SessionManager
import core.session.entities.GuildConfig
import dev.minn.jda.ktx.await
import discord.interact.GuildManager
import discord.interact.InteractionContext
import discord.interact.message.DiscordMessageProducer
import discord.interact.message.MessageActionAdaptor
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.Region
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple2
import java.util.*

private fun matchLocale(locale: Locale): Language =
    when (locale) {
        Locale.KOREA, Locale.KOREAN -> Language.KOR
        Locale.JAPAN, Locale.JAPANESE -> Language.JPN
        else -> Language.ENG
    }

private fun matchRegion(regions: EnumSet<Region>): Language =
    if (Region.SOUTH_KOREA in regions || Region.VIP_SOUTH_KOREA in regions)
        Language.KOR
    else if (Region.JAPAN in regions || Region.VIP_JAPAN in regions)
        Language.JPN
    else
        Language.ENG

fun guildJoinRouter(context: InteractionContext<GuildJoinEvent>): Mono<Tuple2<InteractionContext<GuildJoinEvent>, Result<ServerJoinReport>>> =
    Mono.zip(context.toMono(), mono { runCatching {
        val matchedLanguage = run {
            val language = matchLocale(context.event.guild.locale)

            if (language == Language.ENG)
                matchRegion(context.event.guild.retrieveRegions().await())
            else language
        }

        val guildConfig = GuildConfig(context.guild.id, language = matchedLanguage)

        SessionManager.updateGuildConfig(context.bot.sessions, context.guild.id, guildConfig)

        val commandInserted = runCatching {
            GuildManager.upsertCommands(context.event.guild, matchedLanguage.container)
        }.isSuccess

        val helpSent = context.event.guild.systemChannel?.run {
            GuildManager.permissionGrantedRun(this, Permission.MESSAGE_SEND) {
                buildHelpSequence(context.bot, guildConfig, { msg -> MessageActionAdaptor(this.sendMessage(msg)) }, DiscordMessageProducer, 0)
                    .run()
            }
        }?.isDefined

        ServerJoinReport(commandInserted, helpSent, context.event.guild.locale.displayName, matchedLanguage)
    } } )
