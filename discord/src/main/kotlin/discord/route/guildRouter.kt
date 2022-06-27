package discord.route

import core.BotContext
import core.assets.Guild
import core.assets.GuildUid
import core.database.DatabaseManager
import core.interact.commands.buildCombinedHelpSequence
import core.interact.i18n.Language
import core.interact.reports.ServerJoinReport
import core.session.SessionManager
import core.session.entities.GuildConfig
import dev.minn.jda.ktx.await
import discord.assets.DISCORD_PLATFORM_ID
import discord.assets.extractId
import discord.interact.GuildManager
import discord.interact.message.DiscordMessageProducer
import discord.interact.message.MessageActionAdaptor
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.Region
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import reactor.core.publisher.Mono
import utils.structs.Option.Empty.getOrElse
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

fun guildJoinRouter(bot: BotContext, event: GuildJoinEvent): Mono<Result<ServerJoinReport>> =
    mono { runCatching {
        val guild = DatabaseManager.retrieveOrInsertGuild(bot.databaseConnection, DISCORD_PLATFORM_ID, event.guild.extractId()) {
            Guild(
                id = GuildUid(UUID.randomUUID()),
                platform = DISCORD_PLATFORM_ID,
                givenId = event.guild.extractId(),
                name = event.guild.name,
            )
        }

        val config = DatabaseManager.fetchGuildConfig(bot.databaseConnection, guild.id)
            .getOrElse {
                val matchedLanguage = run {
                    val language = matchLocale(event.guild.locale)

                    if (language == Language.ENG)
                        matchRegion(event.guild.retrieveRegions().await())
                    else language
                }

                GuildConfig(language = matchedLanguage)
            }

        SessionManager.updateGuildConfig(bot.sessions, guild, config)

        val commandInserted = runCatching {
            GuildManager.upsertCommands(event.guild, config.language.container)
        }.isSuccess

        val helpSent = event.guild.systemChannel?.run {
            GuildManager.permissionGrantedRun(this, Permission.MESSAGE_SEND) {
                buildCombinedHelpSequence(bot, config, { msg -> MessageActionAdaptor(this.sendMessage(msg)) }, DiscordMessageProducer, 0)
                    .run()
            }
        }?.isDefined

        ServerJoinReport(commandInserted, helpSent, event.guild.locale.displayName, config.language)
    } }
