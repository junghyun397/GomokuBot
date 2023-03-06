package core.interact.reports

import utils.assets.LinuxTime

interface Report {

    val comment: String

    var emittedTime: LinuxTime?

    var interactionSource: String?

}
