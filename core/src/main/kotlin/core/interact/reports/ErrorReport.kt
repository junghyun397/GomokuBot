package core.interact.reports

import core.assets.Channel
import kotlin.time.Clock
import kotlin.time.Instant

data class ErrorReport(
    private val error: Throwable,
    override val guild: Channel,
    override var interactionSource: String? = null,
    override var emittedTime: Instant? = null,
    override val commandTime: Instant = Clock.System.now(),
    override var apiTime: Instant? = Clock.System.now(),
) : InteractionReport {

    override val commandName = "ERROR"

    override val comment: String
        get() = "error\t ${error.stackTraceToString()}"

}
