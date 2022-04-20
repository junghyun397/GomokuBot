package discord.interact.message

import core.assets.Message
import core.interact.message.MessageAction
import core.interact.message.MessagePublisher
import discord.assets.extractMessage
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

typealias DiscordButtons = List<ActionRow>

typealias DiscordMessagePublisher = MessagePublisher<net.dv8tion.jda.api.entities.Message, DiscordButtons>

class WebHookActionAdaptor(private val original: WebhookMessageAction<net.dv8tion.jda.api.entities.Message>) : MessageAction<DiscordButtons> {
    override fun addFile(file: InputStream, name: String) = WebHookActionAdaptor(original.addFile(file, name))
    override fun addButtons(buttons: DiscordButtons) = WebHookActionAdaptor(original.addActionRows(buttons))
    override fun launch() = original.queue()
    override suspend fun retrieve(): Message = suspendCoroutine { control -> original.queue { control.resume(it.extractMessage()) } }
}

class WebHookMessageUpdateActionAdaptor(private val original: WebhookMessageUpdateAction<net.dv8tion.jda.api.entities.Message>) : MessageAction<DiscordButtons> {
    override fun addFile(file: InputStream, name: String) = WebHookMessageUpdateActionAdaptor(original.addFile(file, name))
    override fun addButtons(buttons: DiscordButtons) = WebHookMessageUpdateActionAdaptor(original.setActionRows(buttons))
    override fun launch() = original.queue()
    override suspend fun retrieve(): Message = suspendCoroutine { control -> original.queue { control.resume(it.extractMessage()) } }
}

class MessageActionAdaptor(private val original: net.dv8tion.jda.api.requests.restaction.MessageAction) : MessageAction<DiscordButtons> {
    override fun addFile(file: InputStream, name: String) = MessageActionAdaptor(original.addFile(file, name))
    override fun addButtons(buttons: DiscordButtons) = MessageActionAdaptor(original.setActionRows(buttons))
    override fun launch() = original.queue()
    override suspend fun retrieve(): Message = suspendCoroutine { control -> original.queue { control.resume(it.extractMessage()) } }
}
