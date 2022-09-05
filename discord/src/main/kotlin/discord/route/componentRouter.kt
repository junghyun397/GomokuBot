@file:Suppress("DuplicatedCode")

package discord.route

import core.interact.commands.ResponseFlag
import core.interact.message.PolyPublisherSet
import core.interact.reports.ErrorReport
import core.interact.reports.InteractionReport
import discord.assets.extractMessageRef
import discord.assets.getEventAbbreviation
import discord.interact.InteractionContext
import discord.interact.message.*
import discord.interact.parse.EmbeddableCommand
import discord.interact.parse.parsers.AcceptCommandParser
import discord.interact.parse.parsers.ApplySettingCommandParser
import discord.interact.parse.parsers.RejectCommandParser
import discord.interact.parse.parsers.SetCommandParser
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import utils.lang.component1
import utils.lang.component2
import utils.structs.Option
import utils.structs.asOption
import utils.structs.flatMap
import utils.structs.getOrException

private fun matchAction(prefix: String): Option<EmbeddableCommand> =
    when (prefix) {
        "s" -> Option(SetCommandParser)
        "a" -> Option(AcceptCommandParser)
        "r" -> Option(RejectCommandParser)
        "p" -> Option(ApplySettingCommandParser)
        else -> Option.Empty
    }

fun buttonInteractionRouter(context: InteractionContext<GenericComponentInteractionCreateEvent>): Mono<InteractionReport> =
    Mono.zip(
        context.toMono(),
        context.event.component.id.asOption().flatMap {
            matchAction(
                prefix = it.split("-").first()
            )
        }.toMono()
    )
        .flatMap { (context, maybeParsable) -> Mono.zip(
            context.toMono(),
            mono { maybeParsable.flatMap { parsable ->
                parsable.parseButton(context)
            } }
        ) }
        .filter { (_, maybeCommand) -> maybeCommand.isDefined }
        .map { tuple -> tuple.mapT2 { it.getOrException() } }
        .doOnNext { (context, command) ->
            if (command.responseFlag == ResponseFlag.DEFER) context.event.deferReply().queue()
        }
        .flatMap { (context, command) -> Mono.zip(context.toMono(), mono {
            command.execute(
                bot = context.bot,
                config = context.config,
                guild = context.guild,
                user = context.user,
                producer = DiscordMessageProducer,
                messageRef = context.event.message.extractMessageRef(),
                publishers = when (command.responseFlag) {
                    ResponseFlag.DEFER -> PolyPublisherSet(
                        plain = { msg -> WebHookActionAdaptor(context.event.hook.sendMessage(msg)) },
                        windowed = { msg -> WebHookActionAdaptor(context.event.hook.sendMessage(msg).setEphemeral(true)) },
                        edit = { msg -> WebHookEditActionAdaptor(context.event.hook.editOriginal(msg)) },
                        component = { components -> WebHookComponentEditActionAdaptor(context.event.hook.editOriginalComponents(components)) }
                    )
                    else -> TransMessagePublisherSet(
                        head = PolyPublisherSet(
                            plain = { msg -> ReplyActionAdaptor(context.event.reply(msg)) },
                            windowed = { msg -> ReplyActionAdaptor(context.event.reply(msg).setEphemeral(true)) },
                            edit = { msg -> MessageEditCallbackAdaptor(context.event.editMessage(msg)) },
                            component = { components -> MessageEditCallbackAdaptor(context.event.editComponents(components)) }
                        ),
                        tail = PolyPublisherSet(
                            plain = { msg -> WebHookActionAdaptor(context.event.hook.sendMessage(msg)) },
                            windowed = { msg -> WebHookActionAdaptor(context.event.hook.sendMessage(msg).setEphemeral(true)) },
                            edit = { msg -> WebHookEditActionAdaptor(context.event.hook.editOriginal(msg)) },
                            component = { components -> WebHookComponentEditActionAdaptor(context.event.hook.editOriginalComponents(components)) }
                        )
                    )
                }
            )
        }) }
        .flatMap { (context, result) ->
            mono {
                result.fold(
                    onSuccess = { (io, report) ->
                        export(context, io, context.event.message.extractMessageRef())
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