package interact.reports

import utility.LinuxTime

class CommandReport(override val terminationTime: LinuxTime = LinuxTime(System.currentTimeMillis())) : InteractionReport