package interact.reports

import interact.commands.ParseFailure
import interact.commands.entities.Command
import utility.LinuxTime

class CommandReport(
    private val commandName: String,
    private val comment: String,
    override val terminationTime: LinuxTime = LinuxTime(),
) : InteractionReport {

    override fun toString() = "(${commandName}) $comment"

}

fun Command.toCommandReport(comment: String) =
    CommandReport(this.name, comment)

fun ParseFailure.toCommandReport() =
    CommandReport("PARSE-FAILURE-${this.name}", this.comment)
