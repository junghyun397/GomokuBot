package core.session.entities

import utils.assets.LinuxTime

data class ExpireService(
    val offset: Long,
    val expireAt: LinuxTime,
    val createDate: LinuxTime
) {

    constructor(offset: Long) : this(offset, LinuxTime.nowWithOffset(offset), LinuxTime.now())

    fun next(): ExpireService = this.copy(expireAt = LinuxTime.nowWithOffset(this.offset))

}
