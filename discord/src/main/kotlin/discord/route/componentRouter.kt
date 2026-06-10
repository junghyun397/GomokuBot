@file:Suppress("DuplicatedCode")

package discord.route

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
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import kotlin.time.Clock

private fun matchAction(prefix: Char?): EmbeddableCommand? =
    when (prefix) {
        DiscordMessagingService.IdConvention.SET -> SetCommandParser
        DiscordMessagingService.IdConvention.ACCEPT -> AcceptCommandParser
        DiscordMessagingService.IdConvention.REJECT -> RejectCommandParser
        DiscordMessagingService.IdConvention.APPLY_SETTING -> ApplySettingCommandParser
        DiscordMessagingService.IdConvention.OPENING -> OpeningCommandParser
        DiscordMessagingService.IdConvention.REPLAY_LIST -> ReplayListCommandParser
        else -> null
    }

suspend fun buttonInteractionRouter(context: UserInteractionContext<GenericComponentInteractionCreateEvent>): Report? {
    val parsable = matchAction(context.event.componentId.split("-").first().getOrNull(0))
        ?: return null

    val command = parsable.parseComponent(context)
        ?: return null

    val responseFlag = command.responseFlag

    if (responseFlag is ResponseFlag.Defer) {
        when (responseFlag.edit) {
            true -> context.event.deferEdit().queue()
            else -> context.event.deferReply().queue()
        }
    }

    val messageRef = context.event.message.extractMessageRef()

    return command.execute(
        bot = context.bot,
        config = context.config,
        channel = context.channel,
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
            ErrorReport(throwable, context.channel)
        }
    ).apply {
        interactionSource = context.source
        emittedTime = context.emittedTime
        apiTime = Clock.System.now()
    }
}
