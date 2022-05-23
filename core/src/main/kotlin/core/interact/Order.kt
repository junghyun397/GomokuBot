package core.interact

import core.assets.Message
import core.interact.i18n.LanguageContainer
import core.session.ArchivePolicy
import core.session.entities.GameSession

sealed class Order {
    class UpsertCommands(val container: LanguageContainer) : Order()
    object DeleteSource : Order()
    class BulkDelete(val key: String) : Order()
    class RemoveNavigators(val message: Message, val retainFistEmbed: Boolean = false) : Order()
    class ArchiveSession(val session: GameSession, val policy: ArchivePolicy) : Order()
    object Unit : Order()
}
