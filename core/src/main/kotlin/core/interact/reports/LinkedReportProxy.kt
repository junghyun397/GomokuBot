package core.interact.reports

import utils.assets.LinuxTime

class LinkedReportProxy(
    private val report: Report,
    override val previousReport: Report
) : LinkedReport {

    override var emittedTime: LinuxTime?
        get() = this.report.commandTime
        set(value) {
            this.report.emittedTime = value
            this.previousReport.emittedTime = value
        }

    override val commandTime: LinuxTime
        get() = this.report.commandTime

    override var interactionSource: String?
        get() = this.report.interactionSource
        set(value) {
            this.report.interactionSource = value
            this.previousReport.interactionSource = value
        }

    override val comment: String
        get() = "\t${this.report.comment}\n\t${this.previousReport.comment}\n"

    override fun buildBody() =
        "LinkedReport:\n${this.comment}"

}
