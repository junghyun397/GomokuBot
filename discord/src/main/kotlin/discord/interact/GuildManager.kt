package discord.interact

import core.interact.i18n.LanguageContainer
import core.session.ArchivePolicy
import core.session.entities.GameSession
import discord.assets.JDAGuild
import discord.interact.message.DiscordMessageProducer
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

    fun insertCommands(guild: JDAGuild, languageContainer: LanguageContainer) =
        guild.retrieveCommands()
            .map { commands -> commands.map { it.delete() } }
            .flatMap {
                buildableCommands.fold(guild.updateCommands()) { action, command ->
                    command.buildCommandData(action, languageContainer)
                }
            }
            .queue()

    fun archiveGame(archiveChannel: TextChannel, session: GameSession, archivePolicy: ArchivePolicy) =
        DiscordMessageProducer.sendBoardArchive(session, archivePolicy) { msg -> archiveChannel.sendMessage(msg) }
            .queue()

}
