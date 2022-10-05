package core.interact.reports

import core.assets.Guild
import utils.assets.LinuxTime

data class ErrorReport(
    private val throwable: Throwable,
    override val guild: Guild,
    override var interactionSource: String? = null,
    override var emittedTime: LinuxTime? = null,
    override val commandTime: LinuxTime = LinuxTime.now(),
    override var apiTime: LinuxTime? = LinuxTime.now(),
) : AbstractInteractionReport() {

    override fun toString() = "${super.toString()} error\t ${throwable.stackTraceToString()}"

}
