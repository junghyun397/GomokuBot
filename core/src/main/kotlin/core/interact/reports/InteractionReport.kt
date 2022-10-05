package core.interact.reports

import core.assets.Guild
import utils.assets.LinuxTime

interface InteractionReport {

    val guild: Guild

    val commandTime: LinuxTime

    var interactionSource: String?

    var emittedTime: LinuxTime?

    var apiTime: LinuxTime?

}
