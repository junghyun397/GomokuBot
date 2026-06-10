package core.interact.message

import arrow.core.raise.Effect
import java.io.InputStream

interface MessageBuilder {

    fun addFile(file: InputStream, name: String): MessageBuilder

    fun addComponents(components: MessageComponents): MessageBuilder

    fun launch(): Effect<Nothing, Unit>

    fun retrieve(): Effect<Nothing, SentMessage?>

}
