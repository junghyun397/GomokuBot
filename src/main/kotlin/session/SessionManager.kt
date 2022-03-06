package session

import session.entities.GuildSession

object SessionManager {

    private val guildIdMap: Map<Long, GuildSession> = mutableMapOf()

    fun getGuildSession(guildId: Long): GuildSession =
        this.guildIdMap.getOrElse(guildId) {
            TODO()
        }

}