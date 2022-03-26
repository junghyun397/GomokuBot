package discord.route

import core.interact.reports.GuildJoinReport
import discord.interact.GuildManager
import discord.interact.InteractionContext
import discord.interact.message.DiscordMessageBinder
import discord.interact.message.DiscordMessagePublisher
import discord.interact.message.MessageActionRestActionAdaptor
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple2
import utils.monads.IO

fun guildJoinRouter(context: InteractionContext<GuildJoinEvent>): Mono<Tuple2<InteractionContext<GuildJoinEvent>, Result<GuildJoinReport>>> =
    Mono.zip(context.toMono(), runCatching {
        val commandInserted = run {
            context.event.guild.selfMember.hasPermission(Permission.MESSAGE_SEND)
            GuildManager.insertCommands(context.event.guild, context.guildConfig.language.container)
            true
        }

        val helpSent = context.event.guild.defaultChannel?.let { channel ->
            val messagePublisher: DiscordMessagePublisher = { msg -> MessageActionRestActionAdaptor(channel.sendMessage(msg)) }

            GuildManager.permissionSafeRun(channel, Permission.MESSAGE_SEND) {
                IO { }
                    .flatMap { DiscordMessageBinder.bindAboutBot(context.guildConfig.language.container, messagePublisher) }
                    .flatMap { DiscordMessageBinder.bindCommandGuide(context.guildConfig.language.container, messagePublisher) }
                    .flatMap { DiscordMessageBinder.bindStyleGuide(context.guildConfig.language.container, messagePublisher) }
                    .flatMap { DiscordMessageBinder.bindLanguageGuide(messagePublisher) }
                    .run()
            }
        }?.isDefined

        GuildJoinReport(commandInserted, helpSent)
    }.toMono())
