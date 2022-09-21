package discord.route

import core.BotContext
import core.assets.COLOR_NORMAL_HEX
import core.assets.MessageRef
import core.interact.message.PolyPublisherSet
import core.interact.reports.ErrorReport
import core.interact.reports.InteractionReport
import core.session.SessionManager
import core.session.entities.BoardNavigationState
import core.session.entities.NavigationState
import core.session.entities.PageNavigationState
import dev.minn.jda.ktx.coroutines.await
import discord.assets.extractMessageRef
import discord.assets.getEventAbbreviation
import discord.interact.GuildManager
import discord.interact.InteractionContext
import discord.interact.message.DiscordMessageProducer
import discord.interact.message.MessageActionAdaptor
import discord.interact.message.MessageComponentActionAdaptor
import discord.interact.parse.parsers.FocusCommandParser
import discord.interact.parse.parsers.NavigationCommandParser
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import reactor.core.publisher.Mono
import utils.lang.and
import utils.structs.*

private fun recoverNavigationState(bot: BotContext, message: Message, messageRef: MessageRef): Option<NavigationState> =
    message.embeds.firstOrNull()
        .asOption()
        .flatMap { DiscordMessageProducer.decodePageNavigationState(COLOR_NORMAL_HEX, it.colorRaw, bot.config, messageRef) }
        .onEach { SessionManager.addNavigation(bot.sessions, messageRef, it) }

fun reactionRouter(context: InteractionContext<GenericMessageReactionEvent>): Mono<InteractionReport> {
    val messageRef = context.event.extractMessageRef()

    return mono {
        val maybeParsable = SessionManager.getnavigationState(context.bot.sessions, messageRef)
        .asOption()
        .orElse { recoverNavigationState(context.bot, context.event.retrieveMessage().await(), messageRef) }
        .map { state ->
            state and when (state) {
                is BoardNavigationState -> FocusCommandParser
                is PageNavigationState -> NavigationCommandParser
            }
        }

        maybeParsable.flatMap { (state, parsable) ->
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
                    producer = DiscordMessageProducer,
                    messageRef = messageRef,
                    publishers = PolyPublisherSet(
                        plain = { msg -> MessageActionAdaptor(context.event.channel.sendMessage(msg)) },
                        windowed = { msg -> MessageActionAdaptor(context.event.channel.sendMessage(msg)) },
                        edit = { msg -> MessageActionAdaptor(context.event.channel.editMessageById(messageRef.id.idLong, msg)) },
                        component = { components -> MessageComponentActionAdaptor(context.event.channel.editMessageComponentsById(messageRef.id.idLong, components)) }
                    ),
                ).fold(
                    onSuccess = { (io, report) ->
                        export(context, io, messageRef)
                        report
                    },
                    onFailure = { throwable ->
                        ErrorReport(throwable, context.guild)
                    }
                ).apply {
                    interactionSource = getEventAbbreviation(context.event::class)
                    emittedTime = context.emittedTime
                }
            }
        }
}
