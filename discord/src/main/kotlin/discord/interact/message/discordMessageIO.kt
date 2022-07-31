package discord.interact.message

import core.assets.MessageRef
import core.interact.message.MessageAdaptor
import core.interact.message.MessageIO
import core.interact.message.MessagePublisher
import discord.assets.extractMessage
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import utils.structs.IO
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

typealias DiscordButtons = List<ActionRow>

typealias DiscordMessagePublisher = MessagePublisher<Message, DiscordButtons>

typealias DiscordMessageAction = MessageIO<Message, DiscordButtons>

abstract class DiscordMessageActionAdaptor(private val original: RestAction<*>) : DiscordMessageAction {

    override fun launch() = IO.lift { this.original.queue() }

}

class WebHookActionAdaptor(private val original: WebhookMessageAction<Message>) : DiscordMessageActionAdaptor(original) {

    override fun addFile(file: InputStream, name: String) = WebHookActionAdaptor(original.addFile(file, name))

    override fun addButtons(buttons: DiscordButtons) = WebHookActionAdaptor(original.addActionRows(buttons))

    override suspend fun retrieve(): DiscordMessageAdaptor = suspendCoroutine { control ->
        this.original.queue { control.resume(DiscordMessageAdaptor(it)) }
    }

}

class WebHookUpdateActionAdaptor(
    private val original: WebhookMessageUpdateAction<Message>,
    private val components: List<ActionRow> = emptyList()
) : DiscordMessageActionAdaptor(original) {

    override fun addFile(file: InputStream, name: String) = WebHookUpdateActionAdaptor(original.addFile(file, name), this.components)

    override fun addButtons(buttons: DiscordButtons) = WebHookUpdateActionAdaptor(original, this.components + buttons)

    override fun launch() =
        IO.lift {
            this.original
                .setActionRows(this.components)
                .queue()
        }

    override suspend fun retrieve(): DiscordMessageAdaptor = suspendCoroutine { control ->
        this.original
            .setActionRows(this.components)
            .queue { control.resume(DiscordMessageAdaptor(it)) }
    }

}

class MessageActionAdaptor(
    private val original: net.dv8tion.jda.api.requests.restaction.MessageAction,
    private val components: List<ActionRow> = emptyList()
) : DiscordMessageActionAdaptor(original) {

    override fun addFile(file: InputStream, name: String) = MessageActionAdaptor(original.addFile(file, name), components)

    override fun addButtons(buttons: DiscordButtons) = MessageActionAdaptor(original, components + buttons)

    override fun launch() =
        IO.lift {
            this.original
                .setActionRows(this.components)
                .queue()
        }

    override suspend fun retrieve(): DiscordMessageAdaptor = suspendCoroutine { control ->
        this.original
            .setActionRows(this.components)
            .queue { control.resume(DiscordMessageAdaptor(it)) }
    }

}

class ReplyActionAdaptor(private val original: ReplyCallbackAction) : DiscordMessageActionAdaptor(original) {

    override fun addFile(file: InputStream, name: String) = ReplyActionAdaptor(original.addFile(file, name))

    override fun addButtons(buttons: DiscordButtons) = ReplyActionAdaptor(original.addActionRows(buttons))

    override suspend fun retrieve(): DiscordMessageAdaptor = suspendCoroutine { control ->
        this.original.queue { hook -> hook.retrieveOriginal().queue { control.resume(DiscordMessageAdaptor(it)) } }
    }

}

class DiscordMessageAdaptor(override val original: Message) : MessageAdaptor<Message, DiscordButtons>() {

    override val messageRef: MessageRef
        get() = this.original.extractMessage()

    override val buttons: DiscordButtons
        get() = this.original.actionRows

    override fun updateButtons(buttons: DiscordButtons) =
        MessageActionAdaptor(this.original.editMessageComponents(), buttons)

}
