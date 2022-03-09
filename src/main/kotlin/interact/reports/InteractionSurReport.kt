package interact.reports

import utility.GuildId
import utility.LinuxTime
import kotlin.reflect.KClass

data class InteractionSurReport<T : Any>(
    val guildId: GuildId,
    val guildName: String,
    val emittedTime: LinuxTime,
    val eventType: KClass<T>
)
