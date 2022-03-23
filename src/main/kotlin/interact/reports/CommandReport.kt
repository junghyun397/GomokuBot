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

fun Command.asCommandReport(comment: String) =
    CommandReport(this.command, comment)

fun ParseFailure.asCommandReport() =
    CommandReport("PARSE-FAILURE-${this.name}", this.comment)
