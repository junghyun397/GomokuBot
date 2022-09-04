package core

import java.time.Duration

data class BotConfig(
    val gameExpireOffset: Long = Duration.ofDays(1).toMillis(),
    val gameExpireCycle: Duration = Duration.ofHours(1),

    val requestExpireOffset: Long = Duration.ofMinutes(30).toMillis(),
    val requestExpireCycle: Duration = Duration.ofMinutes(5),

    val navigatorExpireOffset: Long = Duration.ofDays(1).toMillis(),
    val navigateExpireCycle: Duration = Duration.ofSeconds(30),

    val announceUpdateCycle: Duration = Duration.ofMinutes(20),
)
