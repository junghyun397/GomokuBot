package discord.interact

import core.assets.Message
import core.assets.anonymousUser
import core.interact.i18n.LanguageContainer
import core.session.ArchivePolicy
import core.session.entities.AiGameSession
import core.session.entities.GameSession
import core.session.entities.PvpGameSession
import dev.minn.jda.ktx.Message
import dev.minn.jda.ktx.await
import discord.assets.JDAGuild
import discord.interact.message.DiscordMessageProducer
import discord.interact.message.MessageActionAdaptor
import discord.interact.parse.buildableCommands
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.TextChannel
import utils.structs.Option
import java.time.Duration

object GuildManager {

    fun lookupPermission(channel: TextChannel, permission: Permission) =
        channel.guild.selfMember.hasPermission(channel, permission)

    inline fun <T> permissionSafeRun(channel: TextChannel, permission: Permission, block: (TextChannel) -> T): Option<T> =
        if (this.lookupPermission(channel, permission)) Option(block(channel))
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

        val modSession = if (archivePolicy == ArchivePolicy.BY_ANONYMOUS) {
            when (session) {
                is AiGameSession -> session.copy(owner = anonymousUser)
                is PvpGameSession -> session.copy(owner = anonymousUser, opponent = anonymousUser)
            }
        } else session

        DiscordMessageProducer.produceSessionArchive({ msg -> MessageActionAdaptor(archiveChannel.sendMessage(msg)) }, modSession)
            .map { it.launch() }
    }

    suspend fun retrieveJDAMessage(jda: JDA, message: Message): net.dv8tion.jda.api.entities.Message? =
        jda.getGuildById(message.guildId.idLong)
            ?.getTextChannelById(message.channelId.idLong)
            ?.retrieveMessageById(message.id.idLong)
            ?.await()

    fun retainFirstEmbed(message: net.dv8tion.jda.api.entities.Message) {
        message.editMessage(Message(embed = message.embeds.first()))
            .retainFiles(message.attachments) // TODO: JDA v10 API
            .queue()
    }

    fun removeNavigators(message: net.dv8tion.jda.api.entities.Message) {
        val action = if (message.reactions.isNotEmpty())
            message.editMessageComponents()
                .and(
                    message
                        .reactions
                        .map { it.removeReaction() }
                        .reduce { acc, removeAction -> acc.delay(Duration.ofMillis(500)).and(removeAction) }
                )
        else
            message.editMessageComponents()

        action.queue()
    }

}
