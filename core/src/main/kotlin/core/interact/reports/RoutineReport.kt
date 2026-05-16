package core.interact.reports

import kotlin.time.Instant

data class RoutineReport(
    override val comment: String,
    override var emittedTime: Instant?,
    override var apiTime: Instant?,
    override val commandTime: Instant,
    override var interactionSource: String?,
) : Report
