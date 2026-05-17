package core.interact.reports

import core.assets.Channel

interface InteractionReport : Report {

    val commandName: String

    val channel: Channel

    override fun buildBody(): String = "${channel}\t $commandName\t $comment"

    override fun buildTime(): String {
        val apiTime = this.emittedTime
            ?.let { emitted -> this.apiTime?.let { api -> api.toEpochMilliseconds() - emitted.toEpochMilliseconds() - executionTime } }
            ?: 0

        return "${executionTime}/${apiTime}ms"
    }

}
