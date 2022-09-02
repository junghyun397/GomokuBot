package utils.assets

@JvmInline value class URL(val ref: String)

@JvmInline value class LinuxTime(val timestamp: Long = System.currentTimeMillis()) {

    operator fun compareTo(other: LinuxTime): Int = (timestamp - other.timestamp).toInt()

    companion object {

        fun withOffset(offset: Long) = LinuxTime(System.currentTimeMillis() + offset)

    }

}
