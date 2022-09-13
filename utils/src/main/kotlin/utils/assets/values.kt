package utils.assets

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@JvmInline value class URL(val ref: String)

@JvmInline value class LinuxTime(val timestamp: Long = System.currentTimeMillis()) {

    operator fun compareTo(other: LinuxTime): Int = (timestamp - other.timestamp).toInt()

    override fun toString(): String = LocalDateTime
            .ofEpochSecond(this.timestamp / 1000, (this.timestamp % 1000).toInt(), ZoneOffset.UTC)
            .format(ISO_FORMATTER)

    companion object {

        private val ISO_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

        fun withOffset(offset: Long): LinuxTime = LinuxTime(System.currentTimeMillis() + offset)

    }

}
