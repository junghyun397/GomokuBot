package core.interact.reports

import core.assets.Guild
import utils.assets.LinuxTime

interface InteractionReport : Report {

    val commandName: String

    val guild: Guild

    var apiTime: LinuxTime?

    operator fun plus(other: InteractionReport): InteractionReport =
        UnionReport(listOf(this, other), guild, commandTime, interactionSource, emittedTime, apiTime)

    operator fun plus(other: UnionReport): InteractionReport =
        UnionReport(listOf(this) + other.reports, guild, commandTime, interactionSource, emittedTime, apiTime)


    override fun buildBody(): String = "${guild}\t $commandName\t $comment"

    override fun buildTime(): String {
        val apiTime = this.emittedTime
            ?.let { emitted -> this.apiTime?.let { api -> api.timestamp - emitted.timestamp - executionTime } }
            ?: 0

        return "${executionTime}/${apiTime}ms"
    }

}
