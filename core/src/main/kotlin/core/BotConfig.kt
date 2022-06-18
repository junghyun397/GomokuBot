package core

import java.time.Duration

data class BotConfig(
    val gameExpireOffset: Long = Duration.ofDays(1).toMillis(),
    val requestExpireOffset: Long = Duration.ofMinutes(30).toMillis(),
    val navigatorExpireOffset: Long = Duration.ofDays(1).toMillis(),
)
