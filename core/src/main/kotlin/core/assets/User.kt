package core.assets

import java.util.*

sealed interface User {
    val name: String

    val id: UserUid?
    val profileURL: String?

    data class Human(
        override val name: String,
        override val profileURL: String?,
        override val id: UserUid,
        val platform: Short,
        val givenId: UserId,
        val uniqueName: String,
        val announceId: Int?,
    ) : User {

        override fun toString() = "[${this.uniqueName}](${this.id.uuid})"

        val isAnonymous get() = this == ANONYMOUS

    }

    data object GomokuBot : User {

        override val name = "GomokuBot"

        override val id = null
        override val profileURL = null

    }

    companion object {

        val ANONYMOUS = Human(
            name = "Anonymous",
            id = UserUid(UUID.fromString("00000000-0000-0000-0000-000000000000")),
            platform = 0,
            givenId = UserId(0),
            uniqueName = "Anonymous",
            announceId = null,
            profileURL = null
        )
    }

}
