package discord.route

import core.interact.message.PolyPublisherSet
import core.interact.reports.ErrorReport
import core.interact.reports.InteractionReport
import core.session.SessionManager
import core.session.entities.BoardNavigateState
import core.session.entities.PageNavigateState
import discord.assets.extractMessageRef
import discord.interact.GuildManager
import discord.interact.InteractionContext
import discord.interact.message.DiscordMessageProducer
import discord.interact.message.MessageActionAdaptor
import discord.interact.message.MessageComponentActionAdaptor
import discord.interact.parse.parsers.FocusCommandParser
import discord.interact.parse.parsers.NavigateCommandParser
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import reactor.core.publisher.Mono
import utils.lang.and
import utils.structs.asOption
import utils.structs.flatMap
import utils.structs.getOrException
import utils.structs.map

fun reactionRouter(context: InteractionContext<GenericMessageReactionEvent>): Mono<InteractionReport> {
    val messageRef = context.event.extractMessageRef()

    val maybeParsable = SessionManager.getNavigateState(context.bot.sessions, messageRef)
        .asOption()
        .map { state ->
            state and when (state) {
                is BoardNavigateState -> FocusCommandParser
                is PageNavigateState -> NavigateCommandParser
            }
        }

    return mono {
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
                )
            }
        }
}
