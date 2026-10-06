package core.session.entities

import core.assets.MessageRef

class SessionRuntime<T : Expirable>(
    var session: T,
) {

    @Volatile
    var messageRef: MessageRef? = null
        private set

    private var nextPublicationId = 0L

    private var latestPublicationId = 0L

    var boardNavigation: BoardNavigationState? = null

    var undoRequest: SessionRuntime<RequestSession>? = null

    @Volatile
    var closed: Boolean = false
        private set

    fun reserveMessagePublication(): Long = synchronized(this) {
        ++this.nextPublicationId
    }

    fun recordPublishedMessage(publicationId: Long, messageRef: MessageRef): Boolean = synchronized(this) {
        if (this.closed || publicationId <= this.latestPublicationId) return@synchronized false

        this.latestPublicationId = publicationId
        this.messageRef = messageRef
        true
    }

    fun close() = synchronized(this) {
        this.closed = true
        this.boardNavigation = null
    }

}
