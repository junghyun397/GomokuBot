@file:Suppress("DuplicatedCode")

package discord.route

import core.interact.commands.ResponseFlag
import core.interact.message.AdaptivePublisherSet
import discord.ActionLogRecord
import discord.assets.editMessageByMessageRef
import discord.assets.messageRef
import discord.executeAndRecord
import discord.interact.UserInteractionContext
import discord.interact.message.*
import discord.interact.parse.EmbeddableCommand
import discord.interact.parse.parsers.*
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

private fun matchAction(prefix: Char?): EmbeddableCommand? =
    when (prefix) {
        DiscordPlatformService.CompomentIds.SET -> SetCommandParser
        DiscordPlatformService.CompomentIds.ACCEPT -> AcceptCommandParser
        DiscordPlatformService.CompomentIds.REJECT -> RejectCommandParser
        DiscordPlatformService.CompomentIds.APPLY_SETTING -> ApplySettingCommandParser
        DiscordPlatformService.CompomentIds.OPENING -> OpeningCommandParser
        DiscordPlatformService.CompomentIds.REPLAY_LIST -> ReplayListCommandParser
        DiscordPlatformService.CompomentIds.REPLAY -> ReplayCommandParser
        else -> null
    }

suspend fun buttonInteractionRouter(context: UserInteractionContext<GenericComponentInteractionCreateEvent>): List<ActionLogRecord>? {
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

    val result = command.execute(
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
        },
        emittedTime = context.emittedTime,
    )

    return executeAndRecord(context, result)
}
