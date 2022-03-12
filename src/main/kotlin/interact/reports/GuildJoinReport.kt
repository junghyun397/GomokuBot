package interact.reports

import utility.LinuxTime

class GuildJoinReport(
    override val terminationTime: LinuxTime = LinuxTime(System.currentTimeMillis()),
    private val commandInserted: Boolean = true,
    private val helpSent: Boolean = true
) : InteractionReport {

    override fun toString(): String = "commandInserted = $commandInserted helpSent = $helpSent"

}
