package core.assets

import java.util.*

data class Guild(
    val id: GuildUid,
    val platform: Short,
    val givenId: GuildId,
    val name: String,
) {

    override fun toString() = "[$name](${id.uuid})"

}

data class User(
    val id: UserUid,
    val platform: Short,
    val givenId: UserId,
    val name: String,
    val uniqueName: String,
    val announceId: Int?,
    val profileURL: String?
) {

    override fun toString() = "[$uniqueName](${id.uuid})"

}

data class MessageRef(val id: MessageId, val guildId: GuildId, val channelId: ChannelId)

val DUMMY_MESSAGE_REF = MessageRef(MessageId(-1), GuildId(-1), ChannelId(-1))

@JvmInline value class GuildUid(val uuid: UUID)

@JvmInline value class UserUid(val uuid: UUID) {

    val validationKey: String get() = uuid.mostSignificantBits.toString().takeLast(4)

}

@JvmInline value class UserId(val idLong: Long)

@JvmInline value class GuildId(val idLong: Long)

@JvmInline value class MessageId(val idLong: Long)

@JvmInline value class ChannelId(val idLong: Long)
