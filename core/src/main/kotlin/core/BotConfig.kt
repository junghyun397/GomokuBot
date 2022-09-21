package core

import utils.assets.LinuxTime
import java.time.Duration

data class BotConfig(
    val gameExpireOffset: Long = Duration.ofHours(6).toMillis(),
    val gameExpireCycle: Duration = Duration.ofMinutes(10),

    val requestExpireOffset: Long = Duration.ofMinutes(30).toMillis(),
    val requestExpireCycle: Duration = Duration.ofMinutes(1),

    val navigatorExpireOffset: Long = Duration.ofDays(1).toMillis(),
    val navigatorExpireCycle: Duration = Duration.ofMinutes(1),

    val announceUpdateCycle: Duration = Duration.ofMinutes(20),

    val uptime: LinuxTime = LinuxTime.now()
)
