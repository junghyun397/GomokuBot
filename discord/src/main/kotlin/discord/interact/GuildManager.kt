package discord.interact

import core.interact.i18n.LanguageContainer
import discord.interact.command.buildableCommands
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.GuildChannel
import utils.monads.Option

object GuildManager {

    fun lookupPermission(channel: GuildChannel, permission: Permission) =
        channel.guild.selfMember.hasPermission(channel, permission)

    inline fun <T> permissionSafeRun(channel: GuildChannel, permission: Permission, block: (GuildChannel) -> T): Option<T> {
        if (channel.guild.selfMember.hasPermission(channel, permission)) return Option.Some(block(channel))
        return Option.Empty
    }

    fun insertCommands(guild: Guild, languageContainer: LanguageContainer) =
        buildableCommands.fold(guild.updateCommands()) { action, command ->
            command.buildCommandData(action, languageContainer)
        }.queue()

}
