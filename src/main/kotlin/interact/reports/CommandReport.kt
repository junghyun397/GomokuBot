package interact.reports

import utility.LinuxTime

class CommandReport(
    override val terminationTime: LinuxTime = LinuxTime(System.currentTimeMillis()),
    private val review: String = ""
) : InteractionReport {

    override fun toString() = review

}
