package core.assets

sealed interface User {
    val name: String

    val profileURL: String?

    data class Human(
        override val name: String,
        override val profileURL: String?,
        val id: UserUid,
        val platform: Short,
        val givenId: UserId,
        val uniqueName: String,
        val announceId: Int?,
    ) : User {

        override fun toString() = "[${this.uniqueName}](${this.id.uuid})"

    }

    data object Anonymous : User {
        override val name = "Anonymous"

        override val profileURL = null
    }

    data object GomokuBot : User {
        override val name = "GomokuBot"

        override val profileURL = null
    }

}

val User.humanId: UserUid?
    get() = when (this) {
        is User.Human -> this.id
        User.Anonymous, User.GomokuBot -> null
    }
