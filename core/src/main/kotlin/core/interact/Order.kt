package core.interact

import core.assets.Message
import core.session.ArchivePolicy
import core.session.entities.GameSession

sealed class Order {
    object RefreshCommands : Order()
    object DeleteSource : Order()
    class BulkDelete(val messages: Array<Message>) : Order()
    class ArchiveSession(val session: GameSession, val policy: ArchivePolicy) : Order()
    object Unit : Order()
}
