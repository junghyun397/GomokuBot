package interact.reports

import utility.LinuxTime

class GuildJoinReport(
    override val terminationTime: LinuxTime = LinuxTime(System.currentTimeMillis()),
    val commandInserted: Boolean = true,
    val helpSent: Boolean = true
) : InteractionReport