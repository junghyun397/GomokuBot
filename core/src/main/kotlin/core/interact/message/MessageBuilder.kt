package core.interact.message

import utils.structs.IO
import utils.structs.Option
import java.io.InputStream

interface MessageBuilder<A, B> {

    fun addFile(file: InputStream, name: String): MessageBuilder<A, B>

    fun addComponents(components: B): MessageBuilder<A, B>

    fun launch(): IO<Unit>

    fun retrieve(): IO<Option<MessageAdaptor<A, B>>>

}
