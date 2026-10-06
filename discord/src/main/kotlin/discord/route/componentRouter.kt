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
        DiscordComponentIds.SET -> SetCommandParser
        DiscordComponentIds.ACCEPT, DiscordComponentIds.REJECT -> ResponseCommandParser
        DiscordComponentIds.APPLY_SETTING -> ApplySettingCommandParser
        DiscordComponentIds.OPENING -> OpeningCommandParser
        DiscordComponentIds.REPLAY_LIST -> ReplayListCommandParser
        DiscordComponentIds.REPLAY -> ReplayCommandParser
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
    val platform = DiscordPlatformService(context.shardManager, context.discordConfig, context.jdaChannel)

    val result = command.execute(
        bot = context.bot,
        config = context.config,
        channel = context.channel,
        user = context.user,
        service = platform,
        publishers = when (responseFlag) {
            is ResponseFlag.Defer -> AdaptivePublisherSet(
                plain = discordPublisher { msg -> MessageCreateAdaptor(context.event.hook.sendMessage(msg.buildCreate())) },
                windowed = discordPublisher { msg -> MessageCreateAdaptor(context.event.hook.sendMessage(msg.buildCreate()).setEphemeral(true)) },
                editSelf = discordPublisher { msg -> MessageEditAdaptor(context.event.hook.editOriginal(msg.buildEdit())) },
                editGlobal = { ref -> discordPublisher { msg -> context.jdaChannel.editMessageByMessageRef(ref, msg.buildEdit()) } },
                selfRef = messageRef.takeIf { responseFlag.edit },
            )
            else -> TransMessagePublisherSet(
                selfRef = messageRef,
                head = AdaptivePublisherSet(
                    plain = discordPublisher { msg -> WebHookMessageCreateAdaptor(context.event.reply(msg.buildCreate())) },
                    windowed = discordPublisher { msg -> WebHookMessageCreateAdaptor(context.event.reply(msg.buildCreate()).setEphemeral(true)) },
                    editSelf = discordPublisher { msg -> WebHookMessageEditAdaptor(context.event.editMessage(msg.buildEdit())) },
                    editGlobal = { ref -> discordPublisher { msg -> context.jdaChannel.editMessageByMessageRef(ref, msg.buildEdit()) } },
                    selfRef = messageRef
                ),
                tail = AdaptivePublisherSet(
                    plain = discordPublisher { msg -> MessageCreateAdaptor(context.event.hook.sendMessage(msg.buildCreate())) },
                    windowed = discordPublisher { msg -> MessageCreateAdaptor(context.event.hook.sendMessage(msg.buildCreate()).setEphemeral(true)) },
                    editSelf = discordPublisher { msg -> MessageEditAdaptor(context.event.hook.editOriginal(msg.buildEdit())) },
                    editGlobal = { ref -> discordPublisher { msg -> context.jdaChannel.editMessageByMessageRef(ref, msg.buildEdit()) } },
                    selfRef = messageRef
                )
            )
        },
        emittedTime = context.emittedTime,
    )

    return executeAndRecord(context, result)
}
