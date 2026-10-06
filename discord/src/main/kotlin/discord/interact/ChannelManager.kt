package discord.interact

import arrow.core.raise.get
import core.assets.MessageRef
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.interact.message.AppMessage
import dev.minn.jda.ktx.coroutines.await
import discord.assets.JDAChannel
import discord.assets.awaitNullable
import discord.interact.message.DiscordMessagePublisher
import discord.interact.message.MessageCreateAdaptor
import discord.interact.message.discordPublisher
import discord.interact.parse.buildableCommands
import discord.interact.parse.parsers.HelpCommandParser
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.sharding.ShardManager

object ChannelManager {

    fun lookupPermission(channel: GuildMessageChannel, permission: Permission) =
        channel.guild.selfMember.hasPermission(channel, permission)

    inline fun <T> permissionGrantedRun(channel: GuildMessageChannel, permission: Permission, block: () -> T): T? =
        if (this.lookupPermission(channel, permission)) block()
        else null

    inline fun <T> permissionDependedRun(channel: GuildMessageChannel, permission: Permission, onGranted: () -> T, onMissed: () -> T): T =
        if (this.lookupPermission(channel, permission)) onGranted()
        else onMissed()

    fun initGlobalCommand(shardManager: ShardManager) {
        val jda = shardManager.getShardById(0)
            ?: error("Shard 0 is required to upload global commands.")

        HelpCommandParser.buildHelpCommandData(jda.updateCommands(), Language.ENG.container).queue()
    }

    suspend fun upsertCommands(jdaChannel: JDAChannel, container: LanguageContainer) {
        buildableCommands.fold(jdaChannel.updateCommands()) { action, command ->
            command.buildCommandData(action, container)
        }.await()
    }

    suspend fun archiveMessage(archiveSubChannel: MessageChannel, message: AppMessage.BoardArchive) {
        val publisher: DiscordMessagePublisher = discordPublisher { msg -> MessageCreateAdaptor(archiveSubChannel.sendMessage(msg.buildCreate())) }
        publisher(message).launch().get()
    }

    suspend fun retrieveJDAMessage(jda: JDA, messageRef: MessageRef): net.dv8tion.jda.api.entities.Message? =
        jda.getGuildById(messageRef.channelId.idLong)
            ?.getTextChannelById(messageRef.subChannelId.idLong)
            ?.retrieveMessageById(messageRef.id.idLong)
            ?.awaitNullable()

    fun deleteSingle(jdaChannel: JDAChannel, messageRef: MessageRef) {
        val maybeSubChannel = jdaChannel.getTextChannelById(messageRef.subChannelId.idLong)

        maybeSubChannel?.deleteMessageById(messageRef.id.idLong)?.queue()
    }

    fun clearReactions(message: net.dv8tion.jda.api.entities.Message) {
        this.permissionDependedRun(
            message.channel.asGuildMessageChannel(), Permission.MESSAGE_MANAGE,
            onMissed = {
                message.reactions
                    .map { it.removeReaction(message.jda.selfUser) }
                    .takeIf { it.isNotEmpty() }
                    ?.reduce { acc, action -> acc.and(action) }
            },
            onGranted = { message.clearReactions() }
        )?.queue()
    }

}
