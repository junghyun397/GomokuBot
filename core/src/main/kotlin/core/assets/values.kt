package core.assets

import java.util.*

data class Channel(
    val id: ChannelUid,
    val platform: Short,
    val givenId: ChannelId,
    val name: String,
) {

    override fun toString() = "[$name](${id.uuid})"

}

data class MessageRef(val id: MessageId, val channelId: ChannelId, val subChannelId: SubChannelId)

@JvmInline value class ChannelUid(val uuid: UUID)

@JvmInline value class UserUid(val uuid: UUID) {

    val validationKey: String get() = uuid.mostSignificantBits.toString().takeLast(4)

}

@JvmInline value class UserId(val idLong: Long)

@JvmInline value class ChannelId(val idLong: Long)

@JvmInline value class MessageId(val idLong: Long)

@JvmInline value class SubChannelId(val idLong: Long)
