package core

import java.time.Duration

data class BotConfig(
    val gameExpireAfter: Long = Duration.ofHours(6).toMillis(),
    val gameExpireChecks: Duration = Duration.ofMinutes(10),

    val requestExpireAfter: Long = Duration.ofMinutes(30).toMillis(),
    val requestExpireChecks: Duration = Duration.ofMinutes(1),

    val navigatorExpireAfter: Long = Duration.ofDays(1).toMillis(),
    val navigatorExpireChecks: Duration = Duration.ofMinutes(1),

    val announceUpdateChecks: Duration = Duration.ofMinutes(20),
)
