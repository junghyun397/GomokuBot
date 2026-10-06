package discord.route

import core.database.DatabaseConnection
import core.engine.MintakaServer
import core.interact.message.AdaptivePublisherSet
import core.session.SessionPool
import discord.ActionLogRecord
import discord.assets.editMessageByMessageRef
import discord.assets.messageRef
import discord.executeAndRecord
import discord.interact.ChannelManager
import discord.interact.UserInteractionContext
import discord.interact.message.DiscordPlatformService
import discord.interact.message.MessageCreateAdaptor
import discord.interact.message.MessageEditAdaptor
import discord.interact.message.discordPublisher
import discord.interact.parse.parsers.ReactionCommandParser
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent

context(dbConnection: DatabaseConnection, mintakaServer: MintakaServer, sessions: SessionPool)
suspend fun reactionRouter(context: UserInteractionContext<GenericMessageReactionEvent>): List<ActionLogRecord>? {
    val command = ReactionCommandParser.parseReaction(context) ?: return null
    val messageRef = context.event.messageRef()

    if (context.event is MessageReactionAddEvent) {
        ChannelManager.permissionGrantedRun(context.event.channel.asGuildMessageChannel(), Permission.MESSAGE_MANAGE) {
            context.event.reaction.removeReaction(context.event.user!!).queue()
        }
    }

    val platform = DiscordPlatformService(context.shardManager, context.discordConfig, context.jdaChannel)
    val result = context(platform) {
        command.execute(
            config = context.config,
            channel = context.channel,
            user = context.user,
            publishers = AdaptivePublisherSet(
                plain = discordPublisher { msg -> MessageCreateAdaptor(context.event.channel.sendMessage(msg.buildCreate())) },
                windowed = discordPublisher { msg -> MessageCreateAdaptor(context.event.channel.sendMessage(msg.buildCreate())) },
                editSelf = discordPublisher { msg -> MessageEditAdaptor(context.event.channel.editMessageById(messageRef.id.idLong, msg.buildEdit())) },
                editGlobal = { ref -> discordPublisher { msg -> context.jdaChannel.editMessageByMessageRef(ref, msg.buildEdit()) } },
                selfRef = messageRef,
            ),
            emittedTime = context.emittedTime,
        )
    }

    return executeAndRecord(context, result)
}
