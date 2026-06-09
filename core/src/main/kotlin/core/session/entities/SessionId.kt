package core.session.entities

import java.util.UUID

@JvmInline
value class SessionId(val uuid: UUID) {

    companion object {

        fun issue(): SessionId = SessionId(UUID.randomUUID())

    }

}
