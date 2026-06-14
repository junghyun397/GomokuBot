package core.session.entities

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

data class ExpireService(
    val offset: Duration,
    val expireAt: Instant,
    val createDate: Instant
) {

    constructor(offset: Duration) : this(offset, Clock.System.now() + offset, Clock.System.now())

    fun next(): ExpireService = this.copy(expireAt = Clock.System.now() + this.offset)

}
