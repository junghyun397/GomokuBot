package core.interact.reports

import core.assets.Guild
import utils.assets.LinuxTime

data class ErrorReport(
    private val throwable: Throwable,
    override val guild: Guild,
    override var interactionSource: String? = null,
    override var emittedTime: LinuxTime? = null,
    override val terminationTime: LinuxTime = LinuxTime(),
) : AbstractInteractionReport() {

    override fun toString() = "${super.toString()} error\t ${throwable.stackTraceToString()}"

}