package core

import java.time.Duration

data class BotConfig(
    val gameExpireOffset: Long = Duration.ofHours(1).toMillis(),
    val requestExpireOffset: Long = Duration.ofMinutes(30).toMillis(),
    val navigatorExpireOffset: Long = Duration.ofDays(1).toMillis(),

    val officialChannel: String = "https://discord.gg/vq8pkfF"
)
