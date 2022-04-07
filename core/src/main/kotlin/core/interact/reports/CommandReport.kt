package core.interact.reports

import core.assets.User
import core.interact.commands.Command
import utils.assets.LinuxTime

class CommandReport(
    private val commandName: String,
    private val comment: String,
    private val user: User,
    override val terminationTime: LinuxTime = LinuxTime(),
) : InteractionReport {

    override fun toString() = "(${commandName}) $user $comment"

}

fun Command.asCommandReport(comment: String, user: User) =
    CommandReport(this.command, comment, user)
