package core.interact.message

import arrow.core.raise.Effect
import core.assets.MessageRef
import core.assets.User
import core.interact.i18n.LanguageContainer

interface PlatformService {

    val focusWidth: Int
    val focusRange: IntRange

    suspend fun upsertCommands(container: LanguageContainer)

    suspend fun reduceComponents(messageRef: MessageRef, reduceReactions: Boolean, reduceComponents: Boolean)

    suspend fun archive(message: AppMessage.BoardArchive)

    fun formatUser(user: User): String

    fun formatHighlight(text: String): String

    fun formatBold(text: String): String

    suspend fun updateInputBoard(messageRef: MessageRef, input: BoardInput)

    fun attachInputFieldNavigators(message: SentMessage): Effect<Nothing, Unit>

    fun attachBinaryNavigators(message: SentMessage): Effect<Nothing, Unit>

}
