@file:Suppress("FunctionName")

package discord.interact.message

import core.assets.MessageRef
import core.interact.message.*
import discord.assets.DiscordMessageData
import discord.assets.extractMessageRef
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.requests.FluentRestAction
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageCreateRequest
import net.dv8tion.jda.api.utils.messages.MessageEditRequest
import utils.structs.IO
import utils.structs.Option
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

typealias DiscordComponents = List<LayoutComponent>

typealias DiscordMessagePublisher = MessagePublisher<DiscordMessageData, DiscordComponents>

typealias DiscordComponentPublisher = ComponentPublisher<DiscordMessageData, DiscordComponents>

typealias DiscordMessageBuilder = MessageBuilder<DiscordMessageData, DiscordComponents>

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

class MessageCreateAdaptor<T>(private val original: T) : DiscordMessageBuilder
        where T : MessageCreateRequest<T>, T : FluentRestAction<Message, T> {

    override fun addFile(file: InputStream, name: String): DiscordMessageBuilder =
        MessageCreateAdaptor(this.original.addFiles(FileUpload.fromData(file, name)))

    override fun addComponents(components: DiscordComponents): DiscordMessageBuilder =
        MessageCreateAdaptor(components.fold(this.original) { acc, component -> acc.addActionRow(component.actionComponents) })

    override fun launch(): IO<Unit> = IO.effect {
        this.original.queue()
    }

    override fun retrieve(): IO<Option<DiscordMessageAdaptor>> = IO { suspendCoroutine { control ->
        this.original
            .mapToResult()
            .queue { maybeMessage ->
                maybeMessage
                    .onSuccess { control.resume(Option(DiscordMessageAdaptor(it))) }
                    .onFailure { control.resume(Option.Empty) }
            }
    } }

}

class WebHookMessageCreateAdaptor<T>(private val original: T) : DiscordMessageBuilder
        where T : MessageCreateRequest<T>, T : FluentRestAction<InteractionHook, T> {

    override fun addFile(file: InputStream, name: String): DiscordMessageBuilder =
        WebHookMessageCreateAdaptor(this.original.addFiles(FileUpload.fromData(file, name)))

    override fun addComponents(components: DiscordComponents): DiscordMessageBuilder =
        WebHookMessageCreateAdaptor(components.fold(this.original) { acc, component -> acc.addActionRow(component.actionComponents) })

    override fun launch(): IO<Unit> = IO.effect {
        this.original.queue()
    }

    override fun retrieve(): IO<Option<DiscordMessageAdaptor>> = IO { suspendCoroutine { control ->
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
    } }

}

abstract class MessageEditRequestMixin<T : MessageEditRequest<T>>(
    protected open val original: T,
    protected open val files: List<FileUpload> = emptyList(),
    protected open val components: DiscordComponents = emptyList()
) {

    protected fun applyAttachments(): T {
        val fileAttached = if (this.files.isNotEmpty()) {
            this.original.setFiles(this.files)
        } else {
            this.original
        }

        val componentsAttached = if (this.components.isNotEmpty()) {
            fileAttached.setComponents(this.components)
        } else {
            fileAttached
        }

        return componentsAttached
    }

}

data class MessageEditAdaptor<T>(
    override val original: T,
    override val files: List<FileUpload> = emptyList(),
    override val components: DiscordComponents = emptyList(),
) : MessageEditRequestMixin<T>(original, files, components), DiscordMessageBuilder
        where T : MessageEditRequest<T>, T : FluentRestAction<Message, T> {

    override fun addFile(file: InputStream, name: String): DiscordMessageBuilder =
        this.copy(files = this.files + FileUpload.fromData(file, name))

    override fun addComponents(components: DiscordComponents): DiscordMessageBuilder =
        this.copy(components = this.components + components)

    override fun launch(): IO<Unit> = IO.effect {
        this.applyAttachments().queue()
    }

    override fun retrieve(): IO<Option<DiscordMessageAdaptor>> = IO { suspendCoroutine { control ->
        this.applyAttachments()
            .mapToResult()
            .queue { maybeMessage ->
                maybeMessage
                    .onSuccess { control.resume(Option(DiscordMessageAdaptor(it))) }
                    .onFailure { control.resume(Option.Empty) }
            }
    } }

}

data class WebHookMessageEditAdaptor<T>(
    override val original: T,
    override val files: List<FileUpload> = emptyList(),
    override val components: DiscordComponents = emptyList(),
) : MessageEditRequestMixin<T>(original, files, components), DiscordMessageBuilder
        where T : MessageEditRequest<T>, T : FluentRestAction<InteractionHook, T> {

    override fun addFile(file: InputStream, name: String): DiscordMessageBuilder =
        this.copy(files = this.files + FileUpload.fromData(file, name))

    override fun addComponents(components: DiscordComponents): DiscordMessageBuilder =
        this.copy(components = this.components + components)

    override fun launch(): IO<Unit> = IO.effect {
        this.applyAttachments().queue()
    }

    override fun retrieve(): IO<Option<DiscordMessageAdaptor>> = IO { suspendCoroutine { control ->
        this.applyAttachments()
            .queue { hook -> hook
                .retrieveOriginal()
                .mapToResult()
                .queue { maybeMessage ->
                    maybeMessage
                        .onSuccess { control.resume(Option(DiscordMessageAdaptor(it))) }
                        .onFailure { control.resume(Option.Empty) }
                }
            }
    } }

}

class DiscordMessageAdaptor(private val original: Message) : MessageAdaptor<DiscordMessageData, DiscordComponents> {

    override val messageRef: MessageRef by lazy { this.original.extractMessageRef() }

    override val messageData: DiscordMessageData
        get() = DiscordMessageData(
            this.original.contentRaw,
            this.original.embeds,
            this.original.attachments,
            this.original.components,
            this.original.isTTS,
            Option.Some(this.original)
        )

    override fun updateComponents(components: DiscordComponents): MessageEditAdaptor<*> =
        MessageEditAdaptor(this.original.editMessageComponents(), components = components)

}
