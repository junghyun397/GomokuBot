package core.interact.reports

import utils.assets.LinuxTime

interface Report {

    val comment: String

    var emittedTime: LinuxTime?

    var apiTime: LinuxTime?

    val commandTime: LinuxTime

    var interactionSource: String?

    val executionTime: Long
        get() = this.emittedTime?.let { emitted -> this.commandTime.timestamp - emitted.timestamp } ?: 0

    operator fun plus(other: Report): Report =
        LinkedReportProxy(other, this)

    fun buildTime(): String = "${executionTime}ms"

    fun buildBody(): String = comment

    fun buildLog(): String = "$interactionSource\t ${this.buildTime()}\t ${this.buildBody()}"

}
