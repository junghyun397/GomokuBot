package discord.interact.message

import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.interact.message.MessageBuilder
import core.interact.message.MessageComponents
import core.interact.message.SentMessage
import kotlinx.coroutines.suspendCancellableCoroutine
import net.dv8tion.jda.api.components.MessageTopLevelComponent
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.requests.FluentRestAction
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageCreateRequest
import net.dv8tion.jda.api.utils.messages.MessageEditRequest
import utils.lang.replaceIf
import java.io.InputStream
import kotlin.coroutines.resume

typealias DiscordMessageBuilder = MessageBuilder

@Suppress("UNCHECKED_CAST")
fun MessageComponents.asJdaComponents(): List<MessageTopLevelComponent> =
    this as List<MessageTopLevelComponent>

class MessageCreateAdaptor<T>(private val original: T) : DiscordMessageBuilder
        where T : MessageCreateRequest<T>, T : FluentRestAction<Message, T> {

    override fun addFile(file: InputStream, name: String): DiscordMessageBuilder =
        MessageCreateAdaptor(this.original.addFiles(FileUpload.fromData(file, name)))

    override fun addComponents(components: MessageComponents): DiscordMessageBuilder =
        MessageCreateAdaptor(this.original.addComponents(components.asJdaComponents()))

    override fun launch(): Effect<Nothing, Unit> = effect {
        this@MessageCreateAdaptor.original.queue()
    }

    override fun retrieve(): Effect<Nothing, SentMessage?> = effect {
        suspendCancellableCoroutine { control ->
            this@MessageCreateAdaptor.original
                .mapToResult()
                .queue { maybeMessage ->
                    maybeMessage
                        .onSuccess { control.resume(DiscordSentMessage(it)) }
                        .onFailure { control.resume(null) }
                }
        }
    }

}

class WebHookMessageCreateAdaptor<T>(private val original: T) : DiscordMessageBuilder
        where T : MessageCreateRequest<T>, T : FluentRestAction<InteractionHook, T> {

    override fun addFile(file: InputStream, name: String): DiscordMessageBuilder =
        WebHookMessageCreateAdaptor(this.original.addFiles(FileUpload.fromData(file, name)))

    override fun addComponents(components: MessageComponents): DiscordMessageBuilder =
        WebHookMessageCreateAdaptor(this.original.addComponents(components.asJdaComponents()))

    override fun launch(): Effect<Nothing, Unit> = effect {
        this@WebHookMessageCreateAdaptor.original.queue()
    }

    override fun retrieve(): Effect<Nothing, SentMessage?> = effect {
        suspendCancellableCoroutine { control ->
            this@WebHookMessageCreateAdaptor.original
                .queue { hook ->
                    hook
                        .retrieveOriginal()
                        .mapToResult()
                        .queue { maybeMessage ->
                            maybeMessage
                                .onSuccess { control.resume(DiscordSentMessage(it)) }
                                .onFailure { control.resume(null) }
                        }
                }
        }
    }

}

abstract class MessageEditRequestMixin<T : MessageEditRequest<T>>(
    protected open val original: T,
    protected open val files: List<FileUpload> = emptyList(),
    protected open val components: List<MessageTopLevelComponent> = emptyList()
) {

    protected fun applyAttachments(): T =
        this.original
            .replaceIf(this.files.isNotEmpty()) { it.setFiles(this.files) }
            .replaceIf(this.components.isNotEmpty()) { it.setComponents(this.components) }

}

data class MessageEditAdaptor<T>(
    override val original: T,
    override val files: List<FileUpload> = emptyList(),
    override val components: List<MessageTopLevelComponent> = emptyList(),
) : MessageEditRequestMixin<T>(original, files, components), DiscordMessageBuilder
        where T : MessageEditRequest<T>, T : FluentRestAction<Message, T> {

    override fun addFile(file: InputStream, name: String): DiscordMessageBuilder =
        this.copy(files = this.files + FileUpload.fromData(file, name))

    override fun addComponents(components: MessageComponents): DiscordMessageBuilder =
        this.copy(components = this.components + components.asJdaComponents())

    override fun launch(): Effect<Nothing, Unit> = effect {
        this@MessageEditAdaptor.applyAttachments().queue()
    }

    override fun retrieve(): Effect<Nothing, SentMessage?> = effect {
        suspendCancellableCoroutine { control ->
            this@MessageEditAdaptor.applyAttachments()
                .mapToResult()
                .queue { maybeMessage ->
                    maybeMessage
                        .onSuccess { control.resume(DiscordSentMessage(it)) }
                        .onFailure { control.resume(null) }
                }
        }
    }

}

data class WebHookMessageEditAdaptor<T>(
    override val original: T,
    override val files: List<FileUpload> = emptyList(),
    override val components: List<MessageTopLevelComponent> = emptyList(),
) : MessageEditRequestMixin<T>(original, files, components), DiscordMessageBuilder
        where T : MessageEditRequest<T>, T : FluentRestAction<InteractionHook, T> {

    override fun addFile(file: InputStream, name: String): DiscordMessageBuilder =
        this.copy(files = this.files + FileUpload.fromData(file, name))

    override fun addComponents(components: MessageComponents): DiscordMessageBuilder =
        this.copy(components = this.components + components.asJdaComponents())

    override fun launch(): Effect<Nothing, Unit> = effect {
        this@WebHookMessageEditAdaptor.applyAttachments().queue()
    }

    override fun retrieve(): Effect<Nothing, SentMessage?> = effect {
        suspendCancellableCoroutine { control ->
            this@WebHookMessageEditAdaptor.applyAttachments()
                .queue { hook ->
                    hook
                        .retrieveOriginal()
                        .mapToResult()
                        .queue { maybeMessage ->
                            maybeMessage
                                .onSuccess { control.resume(DiscordSentMessage(it)) }
                                .onFailure { control.resume(null) }
                        }
                }
        }
    }

}
