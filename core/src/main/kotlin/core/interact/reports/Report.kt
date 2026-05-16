package core.interact.reports

import kotlin.time.Instant

interface Report {

    val comment: String

    var emittedTime: Instant?

    var apiTime: Instant?

    val commandTime: Instant

    var interactionSource: String?

    val executionTime: Long
        get() = this.emittedTime?.let { emitted -> this.commandTime.toEpochMilliseconds() - emitted.toEpochMilliseconds() } ?: 0

    operator fun plus(other: Report): Report =
        LinkedReportProxy(other, this)

    fun buildTime(): String = "${executionTime}ms"

    fun buildBody(): String = comment

    fun buildLog(): String = "$interactionSource\t ${this.buildTime()}\t ${this.buildBody()}"

}
