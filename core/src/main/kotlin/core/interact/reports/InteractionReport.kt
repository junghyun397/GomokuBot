package core.interact.reports

import core.assets.Guild

interface InteractionReport : Report {

    val commandName: String

    val guild: Guild

    override fun buildBody(): String = "${guild}\t $commandName\t $comment"

    override fun buildTime(): String {
        val apiTime = this.emittedTime
            ?.let { emitted -> this.apiTime?.let { api -> api.timestamp - emitted.timestamp - executionTime } }
            ?: 0

        return "${executionTime}/${apiTime}ms"
    }

}
