package core.assets

data class Guild(val id: GuildId, val name: String) {
    override fun toString() = "[$name](${id.idLong})"
}

data class User(val id: UserId, val name: String, val nameTag: String, val profileURL: String?) {
    override fun toString() = "[$nameTag](${id.idLong})"
}

data class MessageRef(val id: MessageId, val guildId: GuildId, val channelId: ChannelId)

@JvmInline value class GuildId(val idLong: Long)

@JvmInline value class UserId(val idLong: Long)

@JvmInline value class MessageId(val idLong: Long)

@JvmInline value class ChannelId(val idLong: Long)
