package discord.interact

import core.interact.i18n.LanguageContainer
import discord.interact.command.buildableCommands
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.GuildChannel
import utils.monads.Maybe

object GuildManager {

    fun lookupPermission(channel: GuildChannel, permission: Permission) =
        channel.guild.selfMember.hasPermission(channel, permission)

    inline fun <T> permissionSafeRun(channel: GuildChannel, permission: Permission, block: (GuildChannel) -> T): Maybe<T> {
        if (channel.guild.selfMember.hasPermission(channel, permission)) return Maybe.Just(block(channel))
        return Maybe.Nothing
    }

    fun insertCommands(guild: Guild, languageContainer: LanguageContainer) =
        buildableCommands.fold(guild.updateCommands()) { action, command ->
            command.buildCommandData(action, languageContainer)
        }.queue()

}
