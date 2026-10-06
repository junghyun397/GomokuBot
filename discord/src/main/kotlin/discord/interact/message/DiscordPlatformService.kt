package discord.interact.message

import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.assets.*
import core.interact.i18n.LanguageContainer
import core.interact.message.AppMessage
import core.interact.message.BoardInput
import core.interact.message.PlatformService
import core.interact.message.SentMessage
import discord.assets.JDAChannel
import discord.assets.awaitNullable
import discord.assets.subChannelById
import discord.interact.ChannelManager
import discord.interact.DiscordConfig
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.components.MessageTopLevelComponent
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.sharding.ShardManager
import renju.notation.Pos

class DiscordPlatformService(
    private val shardManager: ShardManager,
    private val discordConfig: DiscordConfig? = null,
    private val jdaChannel: JDAChannel? = null,
) : PlatformService {

    override val focusWidth = DiscordMessageRenderer.FOCUS_WIDTH
    override val focusRange = (this.focusWidth / 2)..(Pos.BOARD_BOUND - this.focusWidth / 2)

    private fun requireJdaChannel(): JDAChannel =
        this.jdaChannel ?: error("JDA channel is required for this platform action.")

    private fun requireDiscordConfig(): DiscordConfig =
        this.discordConfig ?: error("Discord config is required for this platform action.")

    override suspend fun upsertCommands(container: LanguageContainer) {
        ChannelManager.upsertCommands(this.requireJdaChannel(), container)
    }

    override suspend fun reduceComponents(messageRef: MessageRef, reduceReactions: Boolean, reduceComponents: Boolean) {
        ChannelManager.retrieveJDAMessage(this.requireJdaChannel().jda, messageRef)?.let { message ->
            if (reduceReactions) ChannelManager.clearReactions(message)
            if (reduceComponents) message.editMessageComponents(emptyList<MessageTopLevelComponent>()).queue()
        }
    }

    override suspend fun archive(message: AppMessage.BoardArchive) {
        ChannelManager.archiveMessage(
            this.shardManager.getTextChannelById(this.requireDiscordConfig().archiveSubChannelId.idLong)!!,
            message,
        )
    }

    override fun formatUser(user: User): String = when (user) {
        is User.Human -> if (user.isAnonymous) user.name else "<@${user.givenId.idLong}>"
        is User.GomokuBot -> "<@${this.shardManager.shards.first().selfUser.idLong}>"
    }

    override fun formatHighlight(text: String) = "`$text`"

    override fun formatBold(text: String) = "**$text**"

    override suspend fun updateInputBoard(messageRef: MessageRef, input: BoardInput) {
        this.requireJdaChannel().subChannelById(messageRef.subChannelId.idLong)
            ?.editMessageComponentsById(messageRef.id.idLong, DiscordMessageRenderer.renderInputField(input))
            ?.awaitNullable()
    }

    private fun attachNavigators(navigators: List<String>, message: SentMessage): Effect<Nothing, Unit> = effect {
        val original = (message as? DiscordSentMessage)?.original ?: return@effect
        if (!original.isEphemeral
            && ChannelManager.lookupPermission(original.guildChannel, Permission.MESSAGE_ADD_REACTION)
            && ChannelManager.lookupPermission(original.guildChannel, Permission.MESSAGE_HISTORY)) {
            navigators.forEach { original.addReaction(Emoji.fromUnicode(it)).awaitNullable() }
        }
    }

    override fun attachInputFieldNavigators(message: SentMessage): Effect<Nothing, Unit> =
        this.attachNavigators(listOf(UNICODE_LEFT, UNICODE_DOWN, UNICODE_UP, UNICODE_RIGHT, UNICODE_FOCUS), message)

    override fun attachBinaryNavigators(message: SentMessage): Effect<Nothing, Unit> =
        this.attachNavigators(listOf(UNICODE_LEFT, UNICODE_RIGHT), message)

}
