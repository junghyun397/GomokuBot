package core.interact.reports

import core.interact.commands.Command
import utils.assets.LinuxTime

class CommandReport(
    private val commandName: String,
    private val comment: String,
    override val terminationTime: LinuxTime = LinuxTime(),
) : InteractionReport {

    override fun toString() = "(${commandName}) $comment"

}

fun Command.asCommandReport(comment: String) =
    CommandReport(this.command, comment)
