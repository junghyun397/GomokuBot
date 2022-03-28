package discord.assets

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import utils.values.GuildId
import utils.values.MessageId
import utils.values.UserId

fun Guild.extractId() = GuildId(this.idLong)

fun User.extractId() = UserId(this.idLong)

fun Message.extractId() = MessageId(this.idLong)
