package core.interact.reports

import core.assets.Channel
import core.interact.commands.InternalCommand
import kotlin.time.Clock
import kotlin.time.Instant

data class InternalCommandReport(
    override val commandName: String,
    override val comment: String,
    override val guild: Channel,
    override var interactionSource: String? = null,
    override var emittedTime: Instant? = null,
    override val commandTime: Instant = Clock.System.now(),
    override var apiTime: Instant? = Clock.System.now(),
) : InteractionReport

fun InternalCommand.writeCommandReport(comment: String, guild: Channel) =
    InternalCommandReport(this.name, comment, guild)
