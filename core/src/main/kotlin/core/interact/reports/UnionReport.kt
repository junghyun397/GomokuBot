package core.interact.reports

import core.assets.Guild
import utils.assets.LinuxTime

class UnionReport(
    val reports: List<AbstractInteractionReport>,
    override val guild: Guild,
    override val commandTime: LinuxTime,
    override var interactionSource: String?,
    override var emittedTime: LinuxTime?,
    override var apiTime: LinuxTime?
) : AbstractInteractionReport() {

    override operator fun plus(other: AbstractInteractionReport): AbstractInteractionReport =
        UnionReport(this.reports + other, guild, commandTime, interactionSource, emittedTime, apiTime)

    override operator fun plus(other: UnionReport): AbstractInteractionReport =
        UnionReport(this.reports + other.reports, guild, commandTime, interactionSource, emittedTime, apiTime)

    override val comment get() = reports.joinToString(prefix = "\t", separator = "\n\t")

    override fun toString(): String {
        this.reports.forEach { report ->
            report.apply {
                interactionSource = this@UnionReport.interactionSource
                emittedTime = this@UnionReport.emittedTime
                apiTime = this@UnionReport.apiTime
            }
        }

        return "UnionReport(${reports.size}):\n${this.comment}"
    }

}
