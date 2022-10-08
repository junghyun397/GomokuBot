@file:Suppress("FunctionName")

package discord.interact.message

import core.assets.MessageRef
import core.interact.message.MessageAdaptor
import core.interact.message.MessageIO
import core.interact.message.MessagePublisher
import core.interact.message.PublisherSet
import discord.assets.extractMessageRef
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.MessageAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import utils.structs.IO
import utils.structs.Option
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

typealias DiscordComponents = List<ActionRow>

typealias DiscordMessagePublisher = MessagePublisher<Message, DiscordComponents>

typealias DiscordMessageIO = MessageIO<Message, DiscordComponents>

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
    private val head: PublisherSet<Message, DiscordComponents>,
    private val tail: PublisherSet<Message, DiscordComponents>,
) : PublisherSet<Message, DiscordComponents> {

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

abstract class RestActionMessageLauncherMixin(private val original: RestAction<*>) : DiscordMessageIO {

    override fun launch() = IO.effect { this.original.queue() }

}

abstract class RestActionMessageDispatcherMixin(private val original: RestAction<Message>) : RestActionMessageLauncherMixin(original) {

    override fun retrieve(): IO<Option<DiscordMessageAdaptor>> = IO { suspendCoroutine { control ->
        this.original
            .mapToResult()
            .queue { maybeMessage ->
                maybeMessage
                    .onSuccess { control.resume(Option(DiscordMessageAdaptor(it))) }
                    .onFailure { Option.Empty }
            }
    } }

}

abstract class RestActionMessageHookDispatcherMixin(private val original: RestAction<InteractionHook>) : RestActionMessageLauncherMixin(original) {

    override fun retrieve(): IO<Option<DiscordMessageAdaptor>> = IO { suspendCoroutine { control ->
        this.original
            .queue { hook -> hook
                .retrieveOriginal()
                .mapToResult()
                .queue { maybeMessage ->
                    maybeMessage
                        .onSuccess { control.resume(Option.cond(!it.isEphemeral) { DiscordMessageAdaptor(it) }) }
                        .onFailure { control.resume(Option.Empty) }
                }
            }
    } }

}

class WebHookActionAdaptor(private val original: WebhookMessageAction<Message>) : RestActionMessageDispatcherMixin(original) {

    override fun addFile(file: InputStream, name: String) = WebHookActionAdaptor(original.addFile(file, name))

    override fun addButtons(buttons: DiscordComponents) = WebHookActionAdaptor(original.addActionRows(buttons))

}

class WebHookEditActionAdaptor(
    private val original: WebhookMessageUpdateAction<Message>,
    private val components: List<ActionRow> = emptyList()
) : RestActionMessageLauncherMixin(original) {

    override fun addFile(file: InputStream, name: String) = WebHookEditActionAdaptor(original.addFile(file, name), this.components)

    override fun addButtons(buttons: DiscordComponents) = WebHookEditActionAdaptor(original, this.components + buttons)

    override fun launch() =
        IO.effect {
            this.original
                .setActionRows(this.components)
                .queue()
        }

    override fun retrieve(): IO<Option<DiscordMessageAdaptor>> = IO { suspendCoroutine { control ->
        this.original
            .setActionRows(this.components)
            .mapToResult()
            .queue { maybeMessage ->
                maybeMessage
                    .onSuccess { control.resume(Option.cond(!it.isEphemeral) { DiscordMessageAdaptor(it) }) }
                    .onFailure { Option.Empty }
            }
    } }

}

class WebHookComponentEditActionAdaptor(
    private val original: WebhookMessageUpdateAction<Message>,
) : RestActionMessageDispatcherMixin(original) {

    override fun addFile(file: InputStream, name: String) = WebHookComponentEditActionAdaptor(original.addFile(file, name))

    override fun addButtons(buttons: DiscordComponents) = this

    override fun retrieve(): IO<Option<DiscordMessageAdaptor>> = IO { suspendCoroutine { control ->
        this.original
            .mapToResult()
            .queue { maybeMessage ->
                maybeMessage
                    .onSuccess { control.resume(Option.cond(!it.isEphemeral) { DiscordMessageAdaptor(it) }) }
                    .onFailure { Option.Empty }
            }
    } }

}

class MessageActionAdaptor(
    private val original: MessageAction,
    private val components: List<ActionRow> = emptyList()
) : RestActionMessageLauncherMixin(original) {

    override fun addFile(file: InputStream, name: String) = MessageActionAdaptor(original.addFile(file, name), components)

    override fun addButtons(buttons: DiscordComponents) = MessageActionAdaptor(original, components + buttons)

    override fun launch() =
        IO.effect {
            this.original
                .setActionRows(this.components)
                .queue()
        }

    override fun retrieve(): IO<Option<DiscordMessageAdaptor>> = IO { suspendCoroutine { control ->
        this.original
            .setActionRows(this.components)
            .mapToResult()
            .queue { maybeMessage ->
                maybeMessage
                    .onSuccess { control.resume(Option.cond(!it.isEphemeral) { DiscordMessageAdaptor(it) }) }
                    .onFailure { Option.Empty }
            }
    } }

}

class MessageComponentActionAdaptor(
    private val original: MessageAction,
) : RestActionMessageDispatcherMixin(original) {

    override fun addFile(file: InputStream, name: String) = MessageComponentActionAdaptor(original.addFile(file, name))

    override fun addButtons(buttons: DiscordComponents) = this

}

class ReplyActionAdaptor(private val original: ReplyCallbackAction) : RestActionMessageHookDispatcherMixin(original) {

    override fun addFile(file: InputStream, name: String) = ReplyActionAdaptor(original.addFile(file, name))

    override fun addButtons(buttons: DiscordComponents) = ReplyActionAdaptor(original.addActionRows(buttons))



}

class MessageEditCallbackAdaptor(private val original: MessageEditCallbackAction) : RestActionMessageHookDispatcherMixin(original) {

    override fun addFile(file: InputStream, name: String) = MessageEditCallbackAdaptor(original.addFile(file, name))

    override fun addButtons(buttons: DiscordComponents) = MessageEditCallbackAdaptor(original.setActionRows(buttons))

}

class DiscordMessageAdaptor(override val original: Message) : MessageAdaptor<Message, DiscordComponents> {

    override val messageRef: MessageRef by lazy { this.original.extractMessageRef() }

    override val buttons: DiscordComponents
        get() = this.original.actionRows

    override fun updateButtons(buttons: DiscordComponents) =
        MessageActionAdaptor(this.original.editMessageComponents(), buttons)

}
