package core.interact

import core.BotContext
import core.assets.Guild
import core.session.entities.GuildConfig
import utils.assets.LinuxTime

interface ExecutionContext {

    val bot: BotContext

    val guild: Guild

    val config: GuildConfig

    val emittedTime: LinuxTime

    val source: String

}
