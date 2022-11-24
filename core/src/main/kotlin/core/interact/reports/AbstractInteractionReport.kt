package core.interact.reports

abstract class AbstractInteractionReport : InteractionReport {

    override fun toString(): String {
        val executionTime = this.emittedTime
            ?.let { emitted -> this.commandTime.timestamp - emitted.timestamp }
            ?: 0

        val apiTime = this.emittedTime
            ?.let { emitted -> this.apiTime?.let { api -> api.timestamp - emitted.timestamp - executionTime } }
            ?: 0

        return "$interactionSource\t ${executionTime}ms/${apiTime}ms\t $guild"
    }

}
