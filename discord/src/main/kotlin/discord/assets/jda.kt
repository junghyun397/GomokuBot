package discord.assets

import core.assets.*
import java.util.*

const val DISCORD_PLATFORM_ID: Short = 1

typealias JDAGuild = net.dv8tion.jda.api.entities.Guild

fun net.dv8tion.jda.api.entities.Message.extractMessageRef() =
    MessageRef(this.extractId(), this.guild.extractId(), this.channel.extractId())

fun net.dv8tion.jda.api.entities.Guild.extractId() = GuildId(this.idLong)

fun net.dv8tion.jda.api.entities.User.extractId() = UserId(this.idLong)

fun net.dv8tion.jda.api.entities.Message.extractId() = MessageId(this.idLong)

fun net.dv8tion.jda.api.entities.Channel.extractId() = ChannelId(this.idLong)

fun net.dv8tion.jda.api.entities.Guild.extractProfile(uid: GuildUid = GuildUid(UUID.randomUUID())) =
    Guild(uid, DISCORD_PLATFORM_ID, this.extractId(), this.name)

fun net.dv8tion.jda.api.entities.User.extractProfile(uid: UserUid = UserUid(UUID.randomUUID())) =
    User(uid, DISCORD_PLATFORM_ID, this.extractId(), this.name, this.asTag, null, this.avatarUrl)
