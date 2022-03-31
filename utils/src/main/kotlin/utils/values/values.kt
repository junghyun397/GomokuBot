package utils.values

@JvmInline
value class LinuxTime(val timestamp: Long = System.currentTimeMillis()) {
    operator fun compareTo(other: LinuxTime): Int = (timestamp - other.timestamp).toInt()
}
