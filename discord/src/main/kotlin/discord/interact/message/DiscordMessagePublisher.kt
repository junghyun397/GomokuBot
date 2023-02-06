@file:Suppress("FunctionName")

package discord.interact.message

import core.interact.message.*

typealias DiscordMessagePublisher = MessagePublisher<DiscordMessageData, DiscordComponents>

typealias DiscordComponentPublisher = ComponentPublisher<DiscordMessageData, DiscordComponents>

fun TransMessagePublisher(head: DiscordMessagePublisher, tail: DiscordMessagePublisher): DiscordMessagePublisher {
    var consumeTail = false

    return { msg ->
        when (consumeTail) {
            true -> tail(msg)
            else -> {
                consumeTail = true
                head(msg)
            }
        }
    }
}

class TransMessagePublisherSet(
    private val head: PublisherSet<DiscordMessageData, DiscordComponents>,
    private val tail: PublisherSet<DiscordMessageData, DiscordComponents>,
) : PublisherSet<DiscordMessageData, DiscordComponents> {

    private var consumeTail = false

    private fun selectSet() = when(this.consumeTail) {
        true -> this.tail
        else -> {
            this.consumeTail = true
            this.head
        }
    }

    override val plain get() = this.selectSet().plain

    override val windowed get() = this.selectSet().windowed

    override val edit get() = this.selectSet().edit

    override val component get() = this.selectSet().component

}
