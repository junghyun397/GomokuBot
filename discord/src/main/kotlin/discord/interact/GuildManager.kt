package discord.interact

import core.assets.MessageRef
import core.assets.anonymousUser
import core.interact.i18n.LanguageContainer
import core.session.ArchivePolicy
import core.session.entities.AiGameSession
import core.session.entities.GameSession
import core.session.entities.PvpGameSession
import dev.minn.jda.ktx.Message
import dev.minn.jda.ktx.await
import discord.assets.JDAGuild
import discord.assets.OFFICIAL_SERVER_ID
import discord.assets.TESTER_ROLE_ID
import discord.interact.message.DiscordMessageProducer
import discord.interact.message.MessageActionAdaptor
import discord.interact.parse.buildableCommands
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import utils.structs.Option
import java.time.Duration

object GuildManager {

    fun lookupPermission(channel: TextChannel, permission: Permission) =
        channel.guild.selfMember.hasPermission(channel, permission)

    suspend fun hasDebugPermission(user: User): Boolean =
        user.jda
            .getGuildById(OFFICIAL_SERVER_ID)!!
            .retrieveMemberById(user.idLong)
            .await()
            ?.roles
            ?.any { it.idLong == TESTER_ROLE_ID }
            ?: false

    inline fun <T> permissionGrantedRun(channel: TextChannel, permission: Permission, block: () -> T): Option<T> =
        if (this.lookupPermission(channel, permission)) Option(block())
        else Option.Empty

    inline fun <T> permissionNotGrantedRun(channel: TextChannel, permission: Permission, block: () -> T): Option<T> =
        if (this.lookupPermission(channel, permission)) Option.Empty
        else Option(block())

    fun upsertCommands(guild: JDAGuild, container: LanguageContainer) {
        guild.updateCommands()

        buildableCommands.fold(guild.updateCommands()) { action, command ->
            command.buildCommandData(action, container)
        }.queue()
    }

    fun archiveSession(archiveChannel: TextChannel, session: GameSession, archivePolicy: ArchivePolicy) {
        if (archivePolicy == ArchivePolicy.PRIVACY) return

        val modSession = when (archivePolicy) {
            ArchivePolicy.BY_ANONYMOUS -> when (session) {
                is AiGameSession -> session.copy(owner = anonymousUser)
                is PvpGameSession -> session.copy(owner = anonymousUser, opponent = anonymousUser)
            }
            else -> session
        }

        DiscordMessageProducer.produceSessionArchive({ msg -> MessageActionAdaptor(archiveChannel.sendMessage(msg)) }, modSession)
            .map { it.launch() }
    }

    suspend fun retrieveJDAMessage(jda: JDA, messageRef: MessageRef): net.dv8tion.jda.api.entities.Message? =
        jda.getGuildById(messageRef.guildId.idLong)
            ?.getTextChannelById(messageRef.channelId.idLong)
            ?.retrieveMessageById(messageRef.id.idLong)
            ?.await()

    fun retainFirstEmbed(message: net.dv8tion.jda.api.entities.Message) {
        message.editMessage(Message(embed = message.embeds.first()))
            .retainFiles(message.attachments) // TODO: JDA v10 API
            .queue()
    }

    fun removeComponents(message: net.dv8tion.jda.api.entities.Message) =
        message.editMessageComponents()
            .queue()

    fun removeReactions(message: net.dv8tion.jda.api.entities.Message) {
        if (message.reactions.isNotEmpty())
            message
                .reactions
                .map { it.removeReaction() }
                .reduce { acc, removeAction -> acc.delay(Duration.ofMillis(500)).and(removeAction) }
                .queue()
    }

}
