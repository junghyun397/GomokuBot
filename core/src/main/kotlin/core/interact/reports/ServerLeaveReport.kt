package core.interact.reports

import core.assets.Guild
import utils.assets.LinuxTime

data class ServerLeaveReport(
    override val guild: Guild,
    override var interactionSource: String? = null,
    override var emittedTime: LinuxTime? = null,
    override val terminationTime: LinuxTime = LinuxTime(),
) : AbstractInteractionReport() {

    override fun toString() = "${super.toString()}\t leave\t goodbye"

}
