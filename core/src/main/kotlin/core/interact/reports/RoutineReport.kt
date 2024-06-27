package core.interact.reports

import utils.assets.LinuxTime

data class RoutineReport(
    override val comment: String,
    override var emittedTime: LinuxTime?,
    override var apiTime: LinuxTime?,
    override val commandTime: LinuxTime,
    override var interactionSource: String?,
) : Report
