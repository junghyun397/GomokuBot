package core.interact

import core.assets.MessageRef
import core.interact.i18n.LanguageContainer
import core.session.entities.ArchivePolicy
import core.session.entities.GameSession

sealed interface Order {
    class UpsertCommands(val container: LanguageContainer) : Order
    data object DeleteSource : Order
    class BulkDelete(val messageRefs: List<MessageRef>) : Order
    class RemoveNavigators(val messageRef: MessageRef, val reduceComponents: Boolean = false) : Order
    class ArchiveSession(val session: GameSession, val policy: ArchivePolicy) : Order
}

val emptyOrders = emptyList<Order>()
