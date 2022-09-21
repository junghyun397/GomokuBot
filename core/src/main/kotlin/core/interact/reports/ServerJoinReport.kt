package core.interact.reports

import core.assets.Guild
import core.interact.i18n.Language
import utils.assets.LinuxTime

data class ServerJoinReport(
    val commandInserted: Boolean,
    val helpSent: Boolean?,
    val defaultRegion: String,
    val matchedLanguage: Language,
    override val guild: Guild,
    override var interactionSource: String? = null,
    override var emittedTime: LinuxTime? = null,
    override val terminationTime: LinuxTime = LinuxTime.now(),
) : AbstractInteractionReport() {

    override fun toString() = "${super.toString()}\t join\t command=$commandInserted, help=$helpSent, region=$defaultRegion, lang=$matchedLanguage"

}
