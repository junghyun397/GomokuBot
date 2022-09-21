package core.interact.reports

import core.assets.Guild
import core.assets.User
import core.interact.commands.Command
import utils.assets.LinuxTime

data class CommandReport(
    val commandName: String,
    val comment: String,
    override val guild: Guild,
    val user: User,
    override var interactionSource: String? = null,
    override var emittedTime: LinuxTime? = null,
    override val terminationTime: LinuxTime = LinuxTime.now(),
) : AbstractInteractionReport() {

    override fun toString() = "${super.toString()}/$user\t $commandName\t $comment"

}

fun Command.asCommandReport(comment: String, guild: Guild, user: User) =
    CommandReport(this.name, comment, guild, user)
