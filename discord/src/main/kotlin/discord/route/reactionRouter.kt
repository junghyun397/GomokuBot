package discord.route

import core.assets.MessageId
import core.assets.MessageRef
import core.interact.message.PolyPublisherSet
import core.interact.reports.CommandReport
import core.session.SessionManager
import core.session.entities.NavigationKind
import discord.assets.extractId
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
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple2
import utils.lang.and
import utils.lang.component1
import utils.lang.component2
import utils.lang.component3
import utils.structs.asOption
import utils.structs.flatMap
import utils.structs.getOrException
import utils.structs.map

fun reactionRouter(context: InteractionContext<GenericMessageReactionEvent>): Mono<Tuple2<InteractionContext<GenericMessageReactionEvent>, Result<CommandReport>>> =
    Mono.zip(
        context.toMono(),
        run {
            val messageRef = MessageRef(
                id = MessageId(context.event.messageIdLong),
                guildId = context.guild.givenId,
                channelId = context.event.channel.extractId()
            )

            SessionManager
                .getNavigateState(context.bot.sessions, messageRef)
                .asOption()
                .map { state ->
                    Triple(
                        state,
                        when (state.navigationKind) {
                            NavigationKind.BOARD -> FocusCommandParser
                            NavigationKind.ABOUT, NavigationKind.SETTINGS -> NavigateCommandParser
                        },
                        messageRef
                    )
                }
        }.toMono()
    )
        .flatMap { (context, maybeParsable) -> Mono.zip(
            context.toMono(),
            mono {
                maybeParsable.flatMap { (state, parsable, messageRef) ->
                    parsable.parseReaction(context, state).map { command ->
                        command and messageRef
                    }
                }
            }
        ) }
        .filter { (_, maybeTuple) -> maybeTuple.isDefined }
        .map { tuple -> tuple.mapT2 { it.getOrException() } }
        .doOnNext { (context, _) ->
            if (context.event is MessageReactionAddEvent) {
                GuildManager.permissionGrantedRun(context.event.channel.asTextChannel(), Permission.MESSAGE_MANAGE) {
                    context.event.reaction.removeReaction(context.event.user!!).queue()
                }
            }
        }
        .flatMap { (context, tuple) ->
            val (command, messageRef) = tuple

            Mono.zip(
                context.toMono(),
                messageRef.toMono(),
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
                    )
                }
            )
        }
        .flatMap { (context, message, result) ->
            Mono.zip(context.toMono(), mono { result.map { (io, report) ->
                export(context, io, message)
                report
            } })
        }
