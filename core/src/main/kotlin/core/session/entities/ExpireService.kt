package core.session.entities

import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

data class ExpireService(
    val offset: Long,
    val expireAt: Instant,
    val createDate: Instant
) {

    constructor(offset: Long) : this(offset, Clock.System.now() + offset.milliseconds, Clock.System.now())

    fun next(): ExpireService = this.copy(expireAt = Clock.System.now() + this.offset.milliseconds)

}
