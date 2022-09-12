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
    mono {
        context.event.component.id
            .asOption()
            .flatMap { matchAction(it.split("-").first()) }
            .flatMap { parsable ->
                parsable.parseButton(context)
            }
    }
        .filter { it.isDefined }
        .doOnNext { command ->
            val responseFlag = command.getOrException().responseFlag

            if (responseFlag is ResponseFlag.Defer) {
                when (responseFlag.edit) {
                    true -> context.event.deferEdit().queue()
                    else -> context.event.deferReply().queue()
                }
            }
        }
        .flatMap { command -> mono {
            val messageRef = context.event.message.extractMessageRef()

            command.getOrException().execute(
                bot = context.bot,
                config = context.config,
                guild = context.guild,
                user = context.user,
                producer = DiscordMessageProducer,
                messageRef = messageRef,
                publishers = when (command.getOrException().responseFlag) {
                    is ResponseFlag.Defer -> PolyPublisherSet(
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
        } }
