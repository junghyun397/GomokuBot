package core.interact.reports

import core.assets.Channel
import core.assets.User
import core.interact.commands.Command
import kotlin.time.Clock
import kotlin.time.Instant

data class CommandReport(
    override val commandName: String,
    override val comment: String,
    override val guild: Channel,
    val user: User,
    override var interactionSource: String? = null,
    override var emittedTime: Instant? = null,
    override val commandTime: Instant = Clock.System.now(),
    override var apiTime: Instant? = null
) : InteractionReport {

    override fun buildBody() = "${guild}/${user}\t $commandName\t $comment"

}

fun Command.writeCommandReport(comment: String, guild: Channel, user: User) =
    CommandReport(this.name, comment, guild, user)
