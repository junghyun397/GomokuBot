package core.interact.reports

import core.assets.Guild
import core.assets.User
import core.interact.commands.Command
import utils.assets.LinuxTime

data class CommandReport(
    override val commandName: String,
    override val comment: String,
    override val guild: Guild,
    val user: User,
    override var interactionSource: String? = null,
    override var emittedTime: LinuxTime? = null,
    override val commandTime: LinuxTime = LinuxTime.now(),
    override var apiTime: LinuxTime? = null
) : InteractionReport {

    override fun buildBody() = "${guild}/${user}\t $commandName\t $comment"

}

fun Command.writeCommandReport(comment: String, guild: Guild, user: User) =
    CommandReport(this.name, comment, guild, user)
