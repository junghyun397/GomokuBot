package discord

import arrow.core.raise.get
import core.interact.ExecutionContext
import core.interact.commands.CommandResult
import core.interact.reports.ActionLog
import core.interact.reports.ErrorActionLog
import kotlin.time.Clock
import kotlin.time.Duration

data class ActionLogRecord(
    val source: String,
    val interactionExecutionTime: Duration,
    val log: ActionLog
) {

    val error: Throwable?
        get() = (this.log as? ErrorActionLog)?.error

    fun buildLog(): String =
        "${this.source}\t${this.log.commandExecutionTime.inWholeMilliseconds}/${this.interactionExecutionTime.inWholeMilliseconds}ms\t ${this.log.buildBody()}"

}

fun List<ActionLog>.toActionLogRecords(
    context: ExecutionContext,
    interactionExecutionTime: Duration
): List<ActionLogRecord> =
    this.map { log ->
        ActionLogRecord(context.source, interactionExecutionTime, log)
    }

suspend fun executeAndRecord(context: ExecutionContext, result: Result<CommandResult>): List<ActionLogRecord> =
    result.fold(
        onSuccess = { result ->
            val instant = Clock.System.now()

            runCatching {
                result.io.get()
            }.fold(
                onSuccess = {
                    result.events.toActionLogRecords(context, Clock.System.now() - instant)
                },
                onFailure = { throwable ->
                    val commandExecutionTime = result.events
                        .maxOfOrNull { it.commandExecutionTime }
                        ?: Duration.ZERO

                    listOf(ErrorActionLog(throwable, commandExecutionTime))
                        .toActionLogRecords(context, Clock.System.now() - instant)
                }
            )
        },
        onFailure = { throwable ->
            throwable.asActionLogRecord(context)
        }
    )

fun Throwable.asActionLogRecord(context: ExecutionContext): List<ActionLogRecord> {
    val commandExecutionTime = Clock.System.now() - context.emittedTime

    return listOf(ErrorActionLog(this, commandExecutionTime))
        .toActionLogRecords(context, Duration.ZERO)
}
