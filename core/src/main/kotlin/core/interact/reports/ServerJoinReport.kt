package core.interact.reports

import core.assets.Guild
import core.interact.i18n.Language
import utils.assets.LinuxTime

data class ServerJoinReport(
    val guild: Guild,
    val commandInserted: Boolean,
    val helpSent: Boolean?,
    val defaultRegion: String,
    val matchedLanguage: Language,
    override val terminationTime: LinuxTime = LinuxTime(),
) : InteractionReport
