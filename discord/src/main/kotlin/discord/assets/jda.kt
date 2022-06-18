package discord.assets

import core.assets.*

typealias JDAGuild = net.dv8tion.jda.api.entities.Guild

fun net.dv8tion.jda.api.entities.Guild.extractGuild() = Guild(this.extractId(), this.name)

fun net.dv8tion.jda.api.entities.User.extractUser() = User(this.extractId(), this.name, this.asTag, this.avatarUrl)

fun net.dv8tion.jda.api.entities.Message.extractMessage() = MessageRef(this.extractId(), this.guild.extractId(), this.textChannel.extractId())

fun net.dv8tion.jda.api.entities.Guild.extractId() = GuildId(this.idLong)

fun net.dv8tion.jda.api.entities.User.extractId() = UserId(this.idLong)

fun net.dv8tion.jda.api.entities.Message.extractId() = MessageId(this.idLong)

fun net.dv8tion.jda.api.entities.TextChannel.extractId() = ChannelId(this.idLong)
