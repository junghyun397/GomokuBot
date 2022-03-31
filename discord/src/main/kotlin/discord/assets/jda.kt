package discord.assets

import core.assets.*
import net.dv8tion.jda.api.entities.Message

fun net.dv8tion.jda.api.entities.Guild.extractGuild() = Guild(this.extractId(), this.name)

fun net.dv8tion.jda.api.entities.User.extractUser() = User(this.extractId(), this.name, this.asTag)

fun net.dv8tion.jda.api.entities.Guild.extractId() = GuildId(this.idLong)

fun net.dv8tion.jda.api.entities.User.extractId() = UserId(this.idLong)

fun Message.extractId() = MessageId(this.idLong)
