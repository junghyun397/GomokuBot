package core.interact.reports

abstract class AbstractInteractionReport : InteractionReport {

    override fun toString(): String {
        val executionTime = this.emittedTime
            ?.let { this.commandTime.timestamp - it.timestamp }
            ?: 0

        val apiTime = this.emittedTime
            ?.let { emitted -> this.apiTime?.let { it.timestamp - emitted.timestamp } }
            ?: 0

        return "$interactionSource\t ${executionTime}ms/${apiTime}ms\t $guild"
    }

}
