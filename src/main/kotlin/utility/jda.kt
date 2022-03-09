package utility

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.requests.restaction.MessageAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction
import java.io.File

@JvmInline
value class GuildId(val id: Long)

@JvmInline
value class UserId(val id: Long)

typealias MessagePublisher = (Message) -> RestActionAdaptor

interface RestActionAdaptor {
    fun addFile(file: File): RestActionAdaptor
    fun addActionRow(vararg actionRows: ActionRow): RestActionAdaptor
    fun queue()
}

class WebHookRestActionAdaptor(private val original: WebhookMessageAction<Message>) : RestActionAdaptor {
    override fun addFile(file: File) = WebHookRestActionAdaptor(original.addFile(file))
    override fun addActionRow(vararg actionRows: ActionRow) = WebHookRestActionAdaptor(original.addActionRows(*actionRows))
    override fun queue() = original.queue()
}

class MessageActionRestActionAdaptor(private val original: MessageAction) : RestActionAdaptor {
    override fun addFile(file: File) = MessageActionRestActionAdaptor(original.addFile(file))
    override fun addActionRow(vararg actionRows: ActionRow) = MessageActionRestActionAdaptor(original.setActionRows(*actionRows))
    override fun queue() = original.queue()
}
