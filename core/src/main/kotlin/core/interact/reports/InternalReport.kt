package core.interact.reports

import core.assets.Channel
import core.interact.commands.InternalCommand
import utils.assets.LinuxTime

data class InternalCommandReport(
    override val commandName: String,
    override val comment: String,
    override val guild: Channel,
    override var interactionSource: String? = null,
    override var emittedTime: LinuxTime? = null,
    override val commandTime: LinuxTime = LinuxTime.now(),
    override var apiTime: LinuxTime? = LinuxTime.now(),
) : InteractionReport

fun InternalCommand.writeCommandReport(comment: String, guild: Channel) =
    InternalCommandReport(this.name, comment, guild)
