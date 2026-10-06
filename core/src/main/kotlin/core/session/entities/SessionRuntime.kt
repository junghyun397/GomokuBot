package core.session.entities

import core.assets.MessageRef

class SessionRuntime<T : Expirable> internal constructor(
    session: T,
) {

    var session: T = session
        internal set

    @Volatile
    var messageRef: MessageRef? = null
        private set

    private var nextPublicationId = 0L

    private var latestPublicationId = 0L

    var boardNavigation: BoardNavigationState? = null
        internal set

    var undoRequest: SessionRuntime<RequestSession>? = null
        internal set

    @Volatile
    var closed: Boolean = false
        private set

    internal fun reserveMessagePublication(): Long = synchronized(this) {
        ++this.nextPublicationId
    }

    internal fun recordPublishedMessage(publicationId: Long, messageRef: MessageRef): Boolean = synchronized(this) {
        if (this.closed || publicationId <= this.latestPublicationId) return@synchronized false

        this.latestPublicationId = publicationId
        this.messageRef = messageRef
        true
    }

    internal fun close() = synchronized(this) {
        this.closed = true
        this.boardNavigation = null
    }

}
