package utility

import net.dv8tion.jda.api.entities.Message

typealias MessagePublisher = (Message) -> Unit

@JvmInline
value class GuildId(val id: Long)

@JvmInline
value class UserId(val id: Long)
