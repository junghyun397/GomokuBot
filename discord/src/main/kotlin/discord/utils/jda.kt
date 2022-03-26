package discord.utils

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.components.ActionRow
import utils.values.GuildId
import utils.values.UserId

fun Guild.extractId() = GuildId(this.idLong)

fun User.extractId() = UserId(this.idLong)
