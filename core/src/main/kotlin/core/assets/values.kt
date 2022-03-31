package core.assets

data class Guild(val id: GuildId, val name: String) {
    override fun toString() = "[$name](${id.id})"
}

data class User(val id: UserId, val name: String, val fullName: String) {
    override fun toString() = "[$fullName](${id.id})"
}

@JvmInline
value class GuildId(val id: Long)

@JvmInline
value class UserId(val id: Long)

@JvmInline
value class MessageId(val id: Long)

enum class Order {
    REFRESH_COMMANDS, DELETE_SOURCE, UNIT
}
