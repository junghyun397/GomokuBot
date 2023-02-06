package core.interact.message

import core.assets.MessageRef

interface MessageAdaptor<A, B> {

    val messageRef: MessageRef

    val messageData: A

}
