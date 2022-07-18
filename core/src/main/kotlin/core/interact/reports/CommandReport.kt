package core.interact.reports

import core.assets.User
import core.interact.commands.Command
import utils.assets.LinuxTime

data class CommandReport(
    val commandName: String,
    val comment: String,
    val user: User,
    override val terminationTime: LinuxTime = LinuxTime(),
) : InteractionReport {

    override fun toString() = "(${commandName}) $user $comment"

}

fun Command.asCommandReport(comment: String, user: User) =
    CommandReport(this.name, comment, user)
