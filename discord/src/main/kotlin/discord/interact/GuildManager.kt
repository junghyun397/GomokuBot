package discord.interact

import core.interact.i18n.LanguageContainer
import core.session.ArchivePolicy
import core.session.entities.GameSession
import discord.assets.JDAGuild
import discord.interact.parse.buildableCommands
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.TextChannel
import utils.structs.Option

object GuildManager {

    fun lookupPermission(channel: TextChannel, permission: Permission) =
        channel.guild.selfMember.hasPermission(channel, permission)

    inline fun <T> permissionSafeRun(channel: TextChannel, permission: Permission, block: (TextChannel) -> T): Option<T> {
        if (channel.guild.selfMember.hasPermission(channel, permission)) return Option.Some(block(channel))
        return Option.Empty
    }

    fun insertCommands(guild: JDAGuild, languageContainer: LanguageContainer) =
        buildableCommands.fold(guild.updateCommands()) { action, command ->
            command.buildCommandData(action, languageContainer)
        }.queue()

    fun archiveGame(session: GameSession, archivePolicy: ArchivePolicy): Unit = TODO()

}
