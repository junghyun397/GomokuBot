package core.interact.message

import arrow.core.Option
import arrow.core.raise.Effect
import java.io.InputStream

interface MessageBuilder<A, B> {

    fun addFile(file: InputStream, name: String): MessageBuilder<A, B>

    fun addComponents(components: B): MessageBuilder<A, B>

    fun launch(): Effect<Nothing, Unit>

    fun retrieve(): Effect<Nothing, Option<MessageAdaptor<A, B>>>

}
