package core.interact.reports

import kotlin.time.Instant

class LinkedReportProxy(
    private val report: Report,
    override val previousReport: Report
) : LinkedReport {

    override var emittedTime: Instant?
        get() = this.report.commandTime
        set(value) {
            this.report.emittedTime = value
            this.previousReport.emittedTime = value
        }

    override var apiTime: Instant?
        get() = this.report.apiTime
        set(value) {
            this.report.apiTime = value
            this.previousReport.apiTime = value
        }

    override val commandTime: Instant
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
