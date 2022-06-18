package discord.interact.message

import core.assets.MessageRef
import core.interact.message.MessageAdaptor
import core.interact.message.MessageIO
import core.interact.message.MessagePublisher
import discord.assets.extractMessage
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

typealias DiscordButtons = List<ActionRow>

typealias DiscordMessagePublisher = MessagePublisher<net.dv8tion.jda.api.entities.Message, DiscordButtons>

typealias DiscordMessageAction = MessageIO<net.dv8tion.jda.api.entities.Message, DiscordButtons>

abstract class DiscordMessageActionAdaptor(private val original: RestAction<net.dv8tion.jda.api.entities.Message>) : DiscordMessageAction {

    override fun launch() = this.original.queue()

    override suspend fun retrieve(): DiscordMessageAdaptor = suspendCoroutine { control ->
        this.original.queue { control.resume(DiscordMessageAdaptor(it)) }
    }

}

class WebHookActionAdaptor(private val original: WebhookMessageAction<net.dv8tion.jda.api.entities.Message>) : DiscordMessageActionAdaptor(original) {

    override fun addFile(file: InputStream, name: String) = WebHookActionAdaptor(original.addFile(file, name))

    override fun addButtons(buttons: DiscordButtons) = WebHookActionAdaptor(original.addActionRows(buttons))

}

class MessageActionAdaptor(private val original: net.dv8tion.jda.api.requests.restaction.MessageAction) : DiscordMessageActionAdaptor(original) {

    override fun addFile(file: InputStream, name: String) = MessageActionAdaptor(original.addFile(file, name))

    override fun addButtons(buttons: DiscordButtons) = MessageActionAdaptor(original.setActionRows(buttons))

}

class DiscordMessageAdaptor(override val original: net.dv8tion.jda.api.entities.Message) : MessageAdaptor<net.dv8tion.jda.api.entities.Message, DiscordButtons>() {

    override val messageRef: MessageRef
        get() = this.original.extractMessage()

    override val buttons: DiscordButtons
        get() = this.original.actionRows

    override fun updateButtons(buttons: DiscordButtons) =
        MessageActionAdaptor(this.original.editMessageComponents(buttons))

}
