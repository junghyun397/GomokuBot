package discord.route

import core.BotContext
import core.assets.COLOR_NORMAL_HEX
import core.assets.MessageRef
import core.interact.message.AdaptivePublisherSet
import core.interact.reports.ErrorReport
import core.interact.reports.Report
import core.session.SessionManager
import core.session.entities.BoardNavigationState
import core.session.entities.NavigationState
import core.session.entities.PageNavigationState
import dev.minn.jda.ktx.coroutines.await
import discord.assets.extractMessageRef
import discord.interact.GuildManager
import discord.interact.UserInteractionContext
import discord.interact.message.DiscordMessagingService
import discord.interact.message.MessageCreateAdaptor
import discord.interact.message.MessageEditAdaptor
import discord.interact.parse.parsers.FocusCommandParser
import discord.interact.parse.parsers.NavigationCommandParser
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import reactor.core.publisher.Mono
import utils.assets.LinuxTime
import utils.lang.tuple
import utils.structs.*

private fun recoverNavigationState(bot: BotContext, message: Message, messageRef: MessageRef): Option<NavigationState> =
    message.embeds.firstOrNull()
        .asOption()
        .flatMap { PageNavigationState.decodeFromColor(COLOR_NORMAL_HEX, it.colorRaw, bot.config, messageRef) }
        .onEach { SessionManager.addNavigation(bot.sessions, messageRef, it) }

fun reactionRouter(context: UserInteractionContext<GenericMessageReactionEvent>): Mono<Report> {
    val messageRef = context.event.extractMessageRef()

    return mono {
        SessionManager.getNavigationState(context.bot.sessions, messageRef).asOption()
            .orElse { recoverNavigationState(context.bot, context.event.retrieveMessage().await(), messageRef) }
            .map { state ->
                tuple(state, when (state) {
                    is BoardNavigationState -> FocusCommandParser
                    is PageNavigationState -> NavigationCommandParser
                })
            }
            .flatMap { (state, parsable) ->
                parsable.parseReaction(context, state)
            }
    }
        .filter { it.isDefined }
        .map { it.getOrException() }
        .doOnNext {
            if (context.event is MessageReactionAddEvent) {
                GuildManager.permissionGrantedRun(context.event.channel.asTextChannel(), Permission.MESSAGE_MANAGE) {
                    context.event.reaction.removeReaction(context.event.user!!).queue()
                }
            }
        }
        .flatMap { command ->
            mono {
                command.execute(
                    bot = context.bot,
                    config = context.config,
                    guild = context.guild,
                    user = context.user,
                    service = DiscordMessagingService,
                    messageRef = messageRef,
                    publishers = AdaptivePublisherSet(
                        plain = { msg -> MessageCreateAdaptor(context.event.channel.sendMessage(msg.buildCreate())) },
                        windowed = { msg -> MessageCreateAdaptor(context.event.channel.sendMessage(msg.buildCreate())) },
                        editSelf = { msg -> MessageEditAdaptor(context.event.channel.editMessageById(messageRef.id.idLong, msg.buildEdit())) },
                        component = { components -> MessageEditAdaptor(context.event.channel.editMessageComponentsById(messageRef.id.idLong, components)) },
                        selfRef = messageRef,
                    ),
                ).fold(
                    onSuccess = { (io, report) ->
                        export(context.discordConfig, io, context.jdaGuild, messageRef)
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
        }
}
