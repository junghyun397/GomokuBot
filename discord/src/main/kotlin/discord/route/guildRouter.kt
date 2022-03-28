package discord.route

import core.interact.reports.GuildJoinReport
import discord.interact.GuildManager
import discord.interact.InteractionContext
import discord.interact.message.DiscordMessageBinder
import discord.interact.message.DiscordMessagePublisher
import discord.interact.message.MessageActionRestActionAdaptor
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple2
import utils.monads.IO

fun guildJoinRouter(context: InteractionContext<GuildJoinEvent>): Mono<Tuple2<InteractionContext<GuildJoinEvent>, Result<GuildJoinReport>>> =
    Mono.zip(context.toMono(), mono { runCatching {
        val commandInserted = run {
            context.event.guild.selfMember.hasPermission(Permission.MESSAGE_SEND)
            GuildManager.insertCommands(context.event.guild, context.guildConfig.language.container)
            true
        }

        val helpSent = context.event.guild.defaultChannel?.let { channel ->
            val messagePublisher: DiscordMessagePublisher = { msg -> MessageActionRestActionAdaptor(channel.sendMessage(msg)) }

            GuildManager.permissionSafeRun(channel, Permission.MESSAGE_SEND) {
                IO { }
                    .flatMap { DiscordMessageBinder.bindAboutBot(messagePublisher, context.guildConfig.language.container).map { it.retrieve() } }
                    .flatMap { DiscordMessageBinder.bindCommandGuide(messagePublisher, context.guildConfig.language.container).map { it.retrieve() } }
                    .flatMap { DiscordMessageBinder.bindStyleGuide(messagePublisher, context.guildConfig.language.container).map { it.retrieve() } }
                    .flatMap { DiscordMessageBinder.bindLanguageGuide(messagePublisher).map { it.launch() } }
                    .run()
            }
        }?.isDefined

        GuildJoinReport(commandInserted, helpSent)
    } } )
