package discord.interact.message

import core.interact.message.MessageAction
import core.interact.message.MessagePublisher
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction
import java.io.File

typealias DiscordButtons = List<ActionRow>

typealias DiscordMessagePublisher = MessagePublisher<Message, DiscordButtons>

abstract class RestActionAdaptor : MessageAction<DiscordButtons> {
    abstract override fun addFile(file: File): RestActionAdaptor
    abstract override fun addButtons(buttons: DiscordButtons): RestActionAdaptor
    abstract override fun send()
}

class WebHookRestActionAdaptor(private val original: WebhookMessageAction<Message>) : RestActionAdaptor() {
    override fun addFile(file: File) = WebHookRestActionAdaptor(original.addFile(file))
    override fun addButtons(buttons: DiscordButtons) = WebHookRestActionAdaptor(original.addActionRows(buttons))
    override fun send() = original.queue()
}

class MessageActionRestActionAdaptor(private val original: net.dv8tion.jda.api.requests.restaction.MessageAction) : RestActionAdaptor() {
    override fun addFile(file: File) = MessageActionRestActionAdaptor(original.addFile(file))
    override fun addButtons(buttons: DiscordButtons) = MessageActionRestActionAdaptor(original.setActionRows(buttons))
    override fun send() = original.queue()
}
