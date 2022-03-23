package interact.reports

import utility.LinuxTime

class GuildJoinReport(
    private val commandInserted: Boolean = true,
    private val helpSent: Boolean? = true,
    override val terminationTime: LinuxTime = LinuxTime(),
) : InteractionReport {

    override fun toString(): String = "commandInserted = $commandInserted, helpSent = $helpSent"

}
