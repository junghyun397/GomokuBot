package interact.reports

import interact.commands.entities.Command
import utility.LinuxTime

class CommandReport(
    override val terminationTime: LinuxTime = LinuxTime(System.currentTimeMillis()),
    private val commandName: String,
    private val comment: String
) : InteractionReport {

    override fun toString() = "(${commandName}) $comment"

    companion object {

        fun ofCommand(command: Command, comment: String) = CommandReport(commandName = command.name, comment = comment)

    }

}
