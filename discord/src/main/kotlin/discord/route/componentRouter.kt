@file:Suppress("DuplicatedCode")

package discord.route

import arrow.core.raise.get
import core.interact.commands.ResponseFlag
import core.interact.message.AdaptivePublisherSet
import core.interact.reports.ErrorReport
import core.interact.reports.Report
import discord.assets.editMessageByMessageRef
import discord.assets.messageRef
import discord.interact.UserInteractionContext
import discord.interact.message.*
import discord.interact.parse.EmbeddableCommand
import discord.interact.parse.parsers.*
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import kotlin.time.Clock

private fun matchAction(prefix: Char?): EmbeddableCommand? =
    when (prefix) {
        DiscordPlatformService.IdConvention.SET -> SetCommandParser
        DiscordPlatformService.IdConvention.ACCEPT -> AcceptCommandParser
        DiscordPlatformService.IdConvention.REJECT -> RejectCommandParser
        DiscordPlatformService.IdConvention.APPLY_SETTING -> ApplySettingCommandParser
        DiscordPlatformService.IdConvention.OPENING -> OpeningCommandParser
        DiscordPlatformService.IdConvention.REPLAY_LIST -> ReplayListCommandParser
        DiscordPlatformService.IdConvention.REPLAY -> ReplayCommandParser
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

    val messageRef = context.event.message.messageRef()
    val platform = DiscordPlatformService(context.discordConfig, context.jdaChannel)

    return command.execute(
        bot = context.bot,
        config = context.config,
        channel = context.channel,
        user = context.user,
        service = platform,
        publishers = when (command.responseFlag) {
            is ResponseFlag.Defer -> AdaptivePublisherSet(
                plain = { msg -> MessageCreateAdaptor(context.event.hook.sendMessage(msg.asDiscordMessageData().buildCreate())) },
                windowed = { msg -> MessageCreateAdaptor(context.event.hook.sendMessage(msg.asDiscordMessageData().buildCreate()).setEphemeral(true)) },
                editSelf = { msg -> MessageEditAdaptor(context.event.hook.editOriginal(msg.asDiscordMessageData().buildEdit())) },
                editGlobal = { ref -> { msg -> context.jdaChannel.editMessageByMessageRef(ref, msg.asDiscordMessageData().buildEdit()) } },
                component = { components -> MessageEditAdaptor(context.event.hook.editOriginalComponents(components.asJdaComponents())) }
            )
            else -> TransMessagePublisherSet(
                head = AdaptivePublisherSet(
                    plain = { msg -> WebHookMessageCreateAdaptor(context.event.reply(msg.asDiscordMessageData().buildCreate())) },
                    windowed = { msg -> WebHookMessageCreateAdaptor(context.event.reply(msg.asDiscordMessageData().buildCreate()).setEphemeral(true)) },
                    editSelf = { msg -> WebHookMessageEditAdaptor(context.event.editMessage(msg.asDiscordMessageData().buildEdit())) },
                    editGlobal = { ref -> { msg -> context.jdaChannel.editMessageByMessageRef(ref, msg.asDiscordMessageData().buildEdit()) } },
                    component = { components -> WebHookMessageEditAdaptor(context.event.editComponents(components.asJdaComponents())) },
                    selfRef = messageRef
                ),
                tail = AdaptivePublisherSet(
                    plain = { msg -> MessageCreateAdaptor(context.event.hook.sendMessage(msg.asDiscordMessageData().buildCreate())) },
                    windowed = { msg -> MessageCreateAdaptor(context.event.hook.sendMessage(msg.asDiscordMessageData().buildCreate()).setEphemeral(true)) },
                    editSelf = { msg -> MessageEditAdaptor(context.event.hook.editOriginal(msg.asDiscordMessageData().buildEdit())) },
                    editGlobal = { ref -> { msg -> context.jdaChannel.editMessageByMessageRef(ref, msg.asDiscordMessageData().buildEdit()) } },
                    component = { components -> MessageEditAdaptor(context.event.hook.editOriginalComponents(components.asJdaComponents())) },
                    selfRef = messageRef
                )
            )
        }
    ).fold(
        onSuccess = { (io, report) ->
            io.get()
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
