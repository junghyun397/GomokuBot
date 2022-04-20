package discord.interact

import core.assets.anonymousUser
import core.interact.i18n.LanguageContainer
import core.session.ArchivePolicy
import core.session.entities.AiGameSession
import core.session.entities.GameSession
import core.session.entities.PvpGameSession
import discord.assets.JDAGuild
import discord.interact.message.DiscordMessageProducer
import discord.interact.message.MessageActionAdaptor
import discord.interact.parse.buildableCommands
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.TextChannel
import utils.structs.Option

object GuildManager {

    fun lookupPermission(channel: TextChannel, permission: Permission) =
        channel.guild.selfMember.hasPermission(channel, permission)

    inline fun <T> permissionSafeRun(channel: TextChannel, permission: Permission, block: (TextChannel) -> T): Option<T> =
        if (this.lookupPermission(channel, permission)) Option.Some(block(channel))
        else Option.Empty

    inline fun <T> permissionNotGrantedRun(channel: TextChannel, permission: Permission, block: () -> T): Option<T> =
        if (this.lookupPermission(channel, permission)) Option.Empty
        else Option.Some(block())

    fun insertCommands(guild: JDAGuild, languageContainer: LanguageContainer) {
        guild.retrieveCommands()
            .map { commands -> commands.map { it.delete() } }
            .queue()

        buildableCommands.fold(guild.updateCommands()) { action, command ->
            command.buildCommandData(action, languageContainer)
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

        DiscordMessageProducer.producePublicBoard({ msg -> MessageActionAdaptor(archiveChannel.sendMessage(msg)) }, modSession)
            .map { it.launch() }
    }

}
