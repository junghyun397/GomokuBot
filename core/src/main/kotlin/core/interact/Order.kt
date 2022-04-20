package core.interact

import core.interact.i18n.LanguageContainer
import core.session.ArchivePolicy
import core.session.entities.GameSession

sealed class Order {
    class RefreshCommands(val container: LanguageContainer) : Order()
    object DeleteSource : Order()
    class BulkDelete(val key: String) : Order()
    class ArchiveSession(val session: GameSession, val policy: ArchivePolicy) : Order()
    object Unit : Order()
}
