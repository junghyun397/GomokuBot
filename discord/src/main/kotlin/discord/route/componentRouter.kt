@file:Suppress("DuplicatedCode")

package discord.route

import core.interact.commands.ResponseFlag
import core.interact.message.AdaptivePublisherSet
import core.interact.reports.ErrorReport
import core.interact.reports.InteractionReport
import discord.assets.editMessageByMessageRef
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
import utils.assets.LinuxTime
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
            .flatMap { rawId ->
                matchAction(rawId.split("-").first())
            }
            .flatMap { parsable ->
                parsable.parseComponent(context)
            }
    }
        .filter { it.isDefined }
        .map { it.getOrException() }
        .doOnNext { command ->
            val responseFlag = command.responseFlag

            if (responseFlag is ResponseFlag.Defer) {
                when (responseFlag.edit) {
                    true -> context.event.deferEdit().queue()
                    else -> context.event.deferReply().queue()
                }
            }
        }
        .flatMap { command -> mono {
            val messageRef = context.event.message.extractMessageRef()

            command.execute(
                bot = context.bot,
                config = context.config,
                guild = context.guild,
                user = context.user,
                producer = DiscordMessageProducer,
                messageRef = messageRef,
                publishers = when (command.responseFlag) {
                    is ResponseFlag.Defer -> AdaptivePublisherSet(
                        plain = { msg -> MessageCreateAdaptor(context.event.hook.sendMessage(msg.buildCreate())) },
                        windowed = { msg -> MessageCreateAdaptor(context.event.hook.sendMessage(msg.buildCreate()).setEphemeral(true)) },
                        editSelf = { msg -> MessageEditAdaptor(context.event.hook.editOriginal(msg.buildEdit())) },
                        editGlobal = { ref -> { msg -> context.jdaGuild.editMessageByMessageRef(ref, msg.buildEdit()) } },
                        component = { components -> MessageEditAdaptor(context.event.hook.editOriginalComponents(components)) }
                    )
                    else -> TransMessagePublisherSet(
                        head = AdaptivePublisherSet(
                            plain = { msg -> WebHookMessageCreateAdaptor(context.event.reply(msg.buildCreate())) },
                            windowed = { msg -> WebHookMessageCreateAdaptor(context.event.reply(msg.buildCreate()).setEphemeral(true)) },
                            editSelf = { msg -> WebHookMessageEditAdaptor(context.event.editMessage(msg.buildEdit())) },
                            editGlobal = { ref -> { msg -> context.jdaGuild.editMessageByMessageRef(ref, msg.buildEdit()) } },
                            component = { components -> WebHookMessageEditAdaptor(context.event.editComponents(components)) },
                            selfRef = messageRef
                        ),
                        tail = AdaptivePublisherSet(
                            plain = { msg -> MessageCreateAdaptor(context.event.hook.sendMessage(msg.buildCreate())) },
                            windowed = { msg -> MessageCreateAdaptor(context.event.hook.sendMessage(msg.buildCreate()).setEphemeral(true)) },
                            editSelf = { msg -> MessageEditAdaptor(context.event.hook.editOriginal(msg.buildEdit())) },
                            editGlobal = { ref -> { msg -> context.jdaGuild.editMessageByMessageRef(ref, msg.buildEdit()) } },
                            component = { components -> MessageEditAdaptor(context.event.hook.editOriginalComponents(components)) },
                            selfRef = messageRef
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
                apiTime = LinuxTime.now()
            }
        } }
