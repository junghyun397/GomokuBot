package session.entities

import net.dv8tion.jda.api.entities.User

data class RequestSession(val user: User, val team: User)