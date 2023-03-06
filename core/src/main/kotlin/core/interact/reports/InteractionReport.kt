package core.interact.reports

import core.assets.Guild
import utils.assets.LinuxTime

interface InteractionReport : Report {

    val guild: Guild

    val commandTime: LinuxTime

    var apiTime: LinuxTime?

}
