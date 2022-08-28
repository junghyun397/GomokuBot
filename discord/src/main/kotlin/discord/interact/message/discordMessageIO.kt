@file:Suppress("FunctionName")

package discord.interact.message

import core.assets.MessageRef
import core.interact.message.MessageAdaptor
import core.interact.message.MessageIO
import core.interact.message.MessagePublisher
import discord.assets.extractMessageRef
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import utils.structs.IO
import utils.structs.Option
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

typealias DiscordButtons = List<ActionRow>

typealias DiscordMessagePublisher = MessagePublisher<Message, DiscordButtons>

typealias DiscordMessageAction = MessageIO<Message, DiscordButtons>

fun TransMessagePublisher(head: DiscordMessagePublisher, tail: DiscordMessagePublisher): DiscordMessagePublisher {
    var consumeHead = false

    return { msg ->
        when (consumeHead) {
            true -> tail(msg)
            else -> {
                consumeHead = true
                head(msg)
            }
        }
    }
}

abstract class DiscordMessageActionAdaptor(private val original: RestAction<*>) : DiscordMessageAction {

    override fun launch() = IO.effect { this.original.queue() }

}

class WebHookActionAdaptor(private val original: WebhookMessageAction<Message>) : DiscordMessageActionAdaptor(original) {

    override fun addFile(file: InputStream, name: String) = WebHookActionAdaptor(original.addFile(file, name))

    override fun addButtons(buttons: DiscordButtons) = WebHookActionAdaptor(original.addActionRows(buttons))

    override suspend fun retrieve(): Option<DiscordMessageAdaptor> = suspendCoroutine { control ->
        this.original
            .mapToResult()
            .queue { maybeMessage ->
                maybeMessage
                    .onSuccess { control.resume(Option(DiscordMessageAdaptor(it))) }
                    .onFailure { Option.Empty }
            }
    }

}

class WebHookEditActionAdaptor(
    private val original: WebhookMessageUpdateAction<Message>,
    private val components: List<ActionRow> = emptyList()
) : DiscordMessageActionAdaptor(original) {

    override fun addFile(file: InputStream, name: String) = WebHookEditActionAdaptor(original.addFile(file, name), this.components)

    override fun addButtons(buttons: DiscordButtons) = WebHookEditActionAdaptor(original, this.components + buttons)

    override fun launch() =
        IO.effect {
            this.original
                .setActionRows(this.components)
                .queue()
        }

    override suspend fun retrieve(): Option<DiscordMessageAdaptor> = suspendCoroutine { control ->
        this.original
            .setActionRows(this.components)
            .mapToResult()
            .queue { maybeMessage ->
                maybeMessage
                    .onSuccess { control.resume(Option(DiscordMessageAdaptor(it))) }
                    .onFailure { Option.Empty }
            }
    }

}

class WebHookComponentEditActionAdaptor(
    private val original: WebhookMessageUpdateAction<Message>,
) : DiscordMessageActionAdaptor(original) {

    override fun addFile(file: InputStream, name: String) = WebHookComponentEditActionAdaptor(original.addFile(file, name))

    override fun addButtons(buttons: DiscordButtons) = this

    override fun launch() =
        IO.effect {
            this.original
                .queue()
        }

    override suspend fun retrieve(): Option<DiscordMessageAdaptor> = suspendCoroutine { control ->
        this.original
            .mapToResult()
            .queue { maybeMessage ->
                maybeMessage
                    .onSuccess { control.resume(Option(DiscordMessageAdaptor(it))) }
                    .onFailure { Option.Empty }
            }
    }

}

class MessageActionAdaptor(
    private val original: net.dv8tion.jda.api.requests.restaction.MessageAction,
    private val components: List<ActionRow> = emptyList()
) : DiscordMessageActionAdaptor(original) {

    override fun addFile(file: InputStream, name: String) = MessageActionAdaptor(original.addFile(file, name), components)

    override fun addButtons(buttons: DiscordButtons) = MessageActionAdaptor(original, components + buttons)

    override fun launch() =
        IO.effect {
            this.original
                .setActionRows(this.components)
                .queue()
        }

    override suspend fun retrieve(): Option<DiscordMessageAdaptor> = suspendCoroutine { control ->
        this.original
            .setActionRows(this.components)
            .mapToResult()
            .queue { maybeMessage ->
                maybeMessage
                    .onSuccess { control.resume(Option(DiscordMessageAdaptor(it))) }
                    .onFailure { Option.Empty }
            }
    }

}

class MessageComponentActionAdaptor(
    private val original: net.dv8tion.jda.api.requests.restaction.MessageAction,
) : DiscordMessageActionAdaptor(original) {

    override fun addFile(file: InputStream, name: String) = MessageComponentActionAdaptor(original.addFile(file, name))

    override fun addButtons(buttons: DiscordButtons) = this

    override fun launch() =
        IO.effect {
            this.original
                .queue()
        }

    override suspend fun retrieve(): Option<DiscordMessageAdaptor> = suspendCoroutine { control ->
        this.original
            .mapToResult()
            .queue { maybeMessage ->
                maybeMessage
                    .onSuccess { control.resume(Option(DiscordMessageAdaptor(it))) }
                    .onFailure { Option.Empty }
            }
    }

}

class ReplyActionAdaptor(private val original: ReplyCallbackAction) : DiscordMessageActionAdaptor(original) {

    override fun addFile(file: InputStream, name: String) = ReplyActionAdaptor(original.addFile(file, name))

    override fun addButtons(buttons: DiscordButtons) = ReplyActionAdaptor(original.addActionRows(buttons))

    override suspend fun retrieve(): Option<DiscordMessageAdaptor> = suspendCoroutine { control ->
        this.original
            .queue { hook -> hook
                .retrieveOriginal()
                .mapToResult()
                .queue { maybeMessage ->
                    maybeMessage
                        .onSuccess { control.resume(Option(DiscordMessageAdaptor(it))) }
                        .onFailure { control.resume(Option.Empty) }
                }
            }
    }

}

class MessageEditCallbackAdaptor(private val original: MessageEditCallbackAction) : DiscordMessageActionAdaptor(original) {

    override fun addFile(file: InputStream, name: String) = MessageEditCallbackAdaptor(original.addFile(file, name))

    override fun addButtons(buttons: DiscordButtons) = MessageEditCallbackAdaptor(original.setActionRows(buttons))

    override suspend fun retrieve(): Option<DiscordMessageAdaptor> = suspendCoroutine { control ->
        this.original
            .queue { hook -> hook
                .retrieveOriginal()
                .mapToResult()
                .queue { maybeMessage ->
                    maybeMessage
                        .onSuccess { control.resume(Option(DiscordMessageAdaptor(it))) }
                        .onFailure { control.resume(Option.Empty) }
                }
            }
    }

}

class DiscordMessageAdaptor(override val original: Message) : MessageAdaptor<Message, DiscordButtons>() {

    override val messageRef: MessageRef
        get() = this.original.extractMessageRef()

    override val buttons: DiscordButtons
        get() = this.original.actionRows

    override fun updateButtons(buttons: DiscordButtons) =
        MessageActionAdaptor(this.original.editMessageComponents(), buttons)

}
