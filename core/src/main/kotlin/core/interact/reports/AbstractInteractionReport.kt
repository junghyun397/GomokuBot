package core.interact.reports

abstract class AbstractInteractionReport : InteractionReport {

    override fun toString(): String {
        val executionTime = this.emittedTime
            ?.let { this.terminationTime.timestamp - it.timestamp }
            ?: 0

        return "$interactionSource\t ${executionTime}ms\t $guild"
    }

}
