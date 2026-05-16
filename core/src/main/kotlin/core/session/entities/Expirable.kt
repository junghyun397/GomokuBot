package core.session.entities

import kotlin.time.Instant

sealed interface Expirable {

    val expireDate: Instant

}
