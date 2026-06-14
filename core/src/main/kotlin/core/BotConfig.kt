package core

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

object BotConfig {
    val gameExpireAfter: Duration = 1.hours
    val gameExpireChecks: Duration = 5.minutes

    val requestExpireAfter: Duration = 3.hours
    val requestExpireChecks: Duration = 5.minutes

    val navigatorExpireAfter: Duration = 1.days
    val navigatorExpireChecks: Duration = 1.days

    val announceUpdateChecks: Duration = 1.hours
}
