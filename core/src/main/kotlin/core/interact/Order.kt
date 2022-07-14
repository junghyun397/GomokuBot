package core.interact

import core.assets.MessageRef
import core.interact.i18n.LanguageContainer
import core.session.ArchivePolicy
import core.session.entities.GameSession

sealed class Order {
    class UpsertCommands(val container: LanguageContainer) : Order()
    object DeleteSource : Order()
    class BulkDelete(val key: String) : Order()
    class RemoveNavigators(val messageRef: MessageRef, val reduceComponents: Boolean = false) : Order()
    class ArchiveSession(val session: GameSession, val policy: ArchivePolicy) : Order()
}
