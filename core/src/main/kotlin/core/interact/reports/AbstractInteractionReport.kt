package core.interact.reports

abstract class AbstractInteractionReport : InteractionReport {

    open operator fun plus(other: AbstractInteractionReport): AbstractInteractionReport =
        UnionReport(listOf(this, other), guild, commandTime, interactionSource, emittedTime, apiTime)

    open operator fun plus(other: UnionReport): AbstractInteractionReport =
        UnionReport(listOf(this) + other.reports, guild, commandTime, interactionSource, emittedTime, apiTime)

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
