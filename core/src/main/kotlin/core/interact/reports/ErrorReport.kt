package core.interact.reports

import core.assets.Guild
import utils.assets.LinuxTime

data class ErrorReport(
    private val error: Throwable,
    override val guild: Guild,
    override var interactionSource: String? = null,
    override var emittedTime: LinuxTime? = null,
    override val commandTime: LinuxTime = LinuxTime.now(),
    override var apiTime: LinuxTime? = LinuxTime.now(),
) : InteractionReport {

    override val commandName = "ERROR"

    override val comment: String
        get() = "error\t ${error.stackTraceToString()}"

}
