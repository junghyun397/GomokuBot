@file:Suppress("DuplicatedCode")

package discord.route

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.toOption
import core.interact.commands.ResponseFlag
import core.interact.message.AdaptivePublisherSet
import core.interact.reports.ErrorReport
import core.interact.reports.Report
import discord.assets.editMessageByMessageRef
import discord.assets.extractMessageRef
import discord.interact.UserInteractionContext
import discord.interact.message.*
import discord.interact.parse.EmbeddableCommand
import discord.interact.parse.parsers.*
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import reactor.core.publisher.Mono
import utils.assets.LinuxTime

private fun matchAction(prefix: Char?): Option<EmbeddableCommand> =
    when (prefix) {
        DiscordMessagingService.IdConvention.SET -> Some(SetCommandParser)
        DiscordMessagingService.IdConvention.ACCEPT -> Some(AcceptCommandParser)
        DiscordMessagingService.IdConvention.REJECT -> Some(RejectCommandParser)
        DiscordMessagingService.IdConvention.APPLY_SETTING -> Some(ApplySettingCommandParser)
        DiscordMessagingService.IdConvention.OPENING -> Some(OpeningCommandParser)
        DiscordMessagingService.IdConvention.REPLAY_LIST -> Some(ReplayListCommandParser)
        DiscordMessagingService.IdConvention.REPLAY -> Some(ReplayCommandParser)
        else -> None
    }

fun buttonInteractionRouter(context: UserInteractionContext<GenericComponentInteractionCreateEvent>): Mono<Report> =
    mono {
        context.event.component.id
            .toOption()
            .flatMap { rawId ->
                matchAction(rawId.split("-").first().getOrNull(0))
            }
            .flatMap { parsable ->
                parsable.parseComponent(context)
            }
    }
        .filter { it.isSome() }
        .map { it.getOrNull()!! }
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
                service = DiscordMessagingService,
                messageRef = messageRef,
                publishers = when (command.responseFlag) {
                    is ResponseFlag.Defer -> AdaptivePublisherSet(
                        plain = { msg -> MessageCreateAdaptor(context.event.hook.sendMessage(msg.buildCreate())) },
                        windowed = { msg -> MessageCreateAdaptor(context.event.hook.sendMessage(msg.buildCreate()).setEphemeral(true)) },
                        editSelf = { msg -> MessageEditAdaptor(context.event.hook.editOriginal(msg.buildEdit())) },
                        editGlobal = { ref -> { msg -> context.jdaChannel.editMessageByMessageRef(ref, msg.buildEdit()) } },
                        component = { components -> MessageEditAdaptor(context.event.hook.editOriginalComponents(components)) }
                    )
                    else -> TransMessagePublisherSet(
                        head = AdaptivePublisherSet(
                            plain = { msg -> WebHookMessageCreateAdaptor(context.event.reply(msg.buildCreate())) },
                            windowed = { msg -> WebHookMessageCreateAdaptor(context.event.reply(msg.buildCreate()).setEphemeral(true)) },
                            editSelf = { msg -> WebHookMessageEditAdaptor(context.event.editMessage(msg.buildEdit())) },
                            editGlobal = { ref -> { msg -> context.jdaChannel.editMessageByMessageRef(ref, msg.buildEdit()) } },
                            component = { components -> WebHookMessageEditAdaptor(context.event.editComponents(components)) },
                            selfRef = messageRef
                        ),
                        tail = AdaptivePublisherSet(
                            plain = { msg -> MessageCreateAdaptor(context.event.hook.sendMessage(msg.buildCreate())) },
                            windowed = { msg -> MessageCreateAdaptor(context.event.hook.sendMessage(msg.buildCreate()).setEphemeral(true)) },
                            editSelf = { msg -> MessageEditAdaptor(context.event.hook.editOriginal(msg.buildEdit())) },
                            editGlobal = { ref -> { msg -> context.jdaChannel.editMessageByMessageRef(ref, msg.buildEdit()) } },
                            component = { components -> MessageEditAdaptor(context.event.hook.editOriginalComponents(components)) },
                            selfRef = messageRef
                        )
                    )
                }
            ).fold(
                onSuccess = { (io, report) ->
                    executeIO(context.discordConfig, io, context.jdaChannel, messageRef)
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
        } }
