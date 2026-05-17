package core.interact.reports

import core.assets.Channel
import core.assets.User
import core.interact.commands.Command
import kotlin.time.Clock
import kotlin.time.Instant

data class CommandReport(
    override val commandName: String,
    override val comment: String,
    override val channel: Channel,
    val user: User.Human,
    override var interactionSource: String? = null,
    override var emittedTime: Instant? = null,
    override val commandTime: Instant = Clock.System.now(),
    override var apiTime: Instant? = null
) : InteractionReport {

    override fun buildBody() = "${channel}/${user}\t $commandName\t $comment"

}

fun Command.writeCommandReport(comment: String, channel: Channel, user: User.Human) =
    CommandReport(this.name, comment, channel, user)
