package discord.interact.message

import core.interact.message.MessageAction
import core.interact.message.MessagePublisher
import discord.assets.extractId
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction
import core.assets.MessageId
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

typealias DiscordButtons = List<ActionRow>

typealias DiscordMessagePublisher = MessagePublisher<Message, DiscordButtons>

class WebHookRestActionAdaptor(private val original: WebhookMessageAction<Message>) : MessageAction<DiscordButtons> {
    override fun addFile(file: File) = WebHookRestActionAdaptor(original.addFile(file))
    override fun addButtons(buttons: DiscordButtons) = WebHookRestActionAdaptor(original.addActionRows(buttons))
    override fun launch() = original.queue()
    override suspend fun retrieve(): MessageId = suspendCoroutine { control -> original.queue { control.resume(it.extractId()) } }
}

class MessageActionRestActionAdaptor(private val original: net.dv8tion.jda.api.requests.restaction.MessageAction) : MessageAction<DiscordButtons> {
    override fun addFile(file: File) = MessageActionRestActionAdaptor(original.addFile(file))
    override fun addButtons(buttons: DiscordButtons) = MessageActionRestActionAdaptor(original.setActionRows(buttons))
    override fun launch() = original.queue()
    override suspend fun retrieve(): MessageId = suspendCoroutine { control -> original.queue {control.resume(it.extractId()) } }
}
