package core.interact.reports

import core.interact.i18n.Language
import utils.assets.LinuxTime

class ServerJoinReport(
    private val commandInserted: Boolean = true,
    private val helpSent: Boolean? = true,
    private val defaultRegion: String = "unrecognized",
    private val matchedLanguage: Language = Language.ENG,
    override val terminationTime: LinuxTime = LinuxTime(),
) : InteractionReport {

    override fun toString(): String = "commandInserted = $commandInserted, " +
            "defaultRegion = $defaultRegion, " +
            "matchedRegion = $matchedLanguage, " +
            "helpSent = $helpSent"

}
