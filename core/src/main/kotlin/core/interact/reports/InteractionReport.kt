package core.interact.reports

import core.assets.Guild
import utils.assets.LinuxTime

interface InteractionReport {

    val guild: Guild

    val terminationTime: LinuxTime

    var interactionSource: String?

    var emittedTime: LinuxTime?

}
