package core.interact.reports

import core.assets.Guild
import utils.assets.LinuxTime

interface InteractionReport : Report {

    val commandName: String

    val guild: Guild

    var apiTime: LinuxTime?

    operator fun plus(other: InteractionReport): InteractionReport =
        when (other) {
            is UnionReport -> UnionReport(other.reports + this, guild, commandTime, interactionSource, emittedTime, apiTime)
            else -> UnionReport(listOf(this, other), guild, commandTime, interactionSource, emittedTime, apiTime)
        }

    override fun buildBody(): String = "${guild}\t $commandName\t $comment"

    override fun buildTime(): String {
        val apiTime = this.emittedTime
            ?.let { emitted -> this.apiTime?.let { api -> api.timestamp - emitted.timestamp - executionTime } }
            ?: 0

        return "${executionTime}/${apiTime}ms"
    }

}
