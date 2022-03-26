package utils.values

@JvmInline
value class GuildId(val id: Long)

@JvmInline
value class UserId(val id: Long)

@JvmInline
value class LinuxTime(val timestamp: Long = System.currentTimeMillis()) {
    operator fun compareTo(other: LinuxTime): Int = (timestamp - other.timestamp).toInt()
}
