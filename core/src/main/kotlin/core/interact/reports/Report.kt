package core.interact.reports

import utils.assets.LinuxTime

interface Report {

    val comment: String

    var emittedTime: LinuxTime?

    val commandTime: LinuxTime

    var interactionSource: String?

    val executionTime: Long
        get() = this.emittedTime?.let { emitted -> this.commandTime.timestamp - emitted.timestamp } ?: 0

    fun buildTime(): String = "${executionTime}ms"

    fun buildBody(): String = comment

    fun buildLog(): String = "$interactionSource\t ${this.buildTime()}\t ${this.buildBody()}"

}
