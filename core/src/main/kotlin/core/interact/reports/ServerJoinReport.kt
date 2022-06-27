package core.interact.reports

import core.interact.i18n.Language
import utils.assets.LinuxTime

data class ServerJoinReport(
    val commandInserted: Boolean,
    val helpSent: Boolean?,
    val defaultRegion: String,
    val matchedLanguage: Language,
    override val terminationTime: LinuxTime = LinuxTime(),
) : InteractionReport
