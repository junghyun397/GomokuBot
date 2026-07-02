package discord.route

import core.BotContext
import core.assets.COLOR_NORMAL_HEX
import core.assets.MessageRef
import core.interact.message.AdaptivePublisherSet
import core.session.MessageManager
import core.session.entities.BoardNavigationState
import core.session.entities.NavigationState
import core.session.entities.PageNavigationState
import dev.minn.jda.ktx.coroutines.await
import discord.ActionLogRecord
import discord.assets.messageRef
import discord.executeAndRecord
import discord.interact.ChannelManager
import discord.interact.UserInteractionContext
import discord.interact.message.*
import discord.interact.parse.parsers.FocusCommandParser
import discord.interact.parse.parsers.NavigationCommandParser
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent

private fun recoverNavigationState(bot: BotContext, message: Message, messageRef: MessageRef): NavigationState? =
    message.embeds.firstOrNull()
        ?.let { PageNavigationState.decodeFromColor(COLOR_NORMAL_HEX, it.colorRaw, messageRef, bot.dbConnection) }
        ?.also { MessageManager.addNavigation(bot.sessions, messageRef, it) }

suspend fun reactionRouter(context: UserInteractionContext<GenericMessageReactionEvent>): List<ActionLogRecord>? {
    val messageRef = context.event.messageRef()

    val state = MessageManager.getNavigationState(context.bot.sessions, messageRef)
        ?: recoverNavigationState(context.bot, context.event.retrieveMessage().await(), messageRef)
        ?: return null

    val parsable = when (state) {
        is BoardNavigationState -> FocusCommandParser
        is PageNavigationState -> NavigationCommandParser
    }

    val command = parsable.parseReaction(context, state)
        ?: return null

    if (context.event is MessageReactionAddEvent) {
        ChannelManager.permissionGrantedRun(context.event.channel.asGuildMessageChannel(), Permission.MESSAGE_MANAGE) {
            context.event.reaction.removeReaction(context.event.user!!).queue()
        }
    }

    val result = command.execute(
        bot = context.bot,
        config = context.config,
        channel = context.channel,
        user = context.user,
        service = DiscordPlatformService(context.discordConfig, context.jdaChannel),
        publishers = AdaptivePublisherSet(
            plain = { msg -> MessageCreateAdaptor(context.event.channel.sendMessage(msg.asDiscordMessageData().buildCreate())) },
            windowed = { msg -> MessageCreateAdaptor(context.event.channel.sendMessage(msg.asDiscordMessageData().buildCreate())) },
            editSelf = { msg -> MessageEditAdaptor(context.event.channel.editMessageById(messageRef.id.idLong, msg.asDiscordMessageData().buildEdit())) },
            component = { components -> MessageEditAdaptor(context.event.channel.editMessageComponentsById(messageRef.id.idLong, components.asJdaComponents())) },
            selfRef = messageRef,
        ),
        emittedTime = context.emittedTime,
    )

    return executeAndRecord(context, result)
}
