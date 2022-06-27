package discord.assets

import core.assets.*
import java.util.*

const val DISCORD_PLATFORM_ID = 1

typealias JDAGuild = net.dv8tion.jda.api.entities.Guild

fun net.dv8tion.jda.api.entities.Message.extractMessage() =
    MessageRef(this.extractId(), this.guild.extractId(), this.textChannel.extractId())

fun net.dv8tion.jda.api.entities.Guild.extractId() = GuildId(this.idLong)

fun net.dv8tion.jda.api.entities.User.extractId() = UserId(this.idLong)

fun net.dv8tion.jda.api.entities.Message.extractId() = MessageId(this.idLong)

fun net.dv8tion.jda.api.entities.TextChannel.extractId() = ChannelId(this.idLong)

fun net.dv8tion.jda.api.entities.User.buildNewProfile() =
    User(UserUid(UUID.randomUUID()), DISCORD_PLATFORM_ID, this.extractId(), this.name, this.asTag, this.avatarUrl)
