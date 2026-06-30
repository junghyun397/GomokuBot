package discord.interact.message

import core.interact.message.MessagePayload
import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.components.MessageTopLevelComponent
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.utils.AttachedFile
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditData
import core.interact.message.PlatformMessage as CoreMessage

data class DiscordMessageData(
    val content: String = "",
    val embeds: List<MessageEmbed> = emptyList(),
    val files: List<AttachedFile> = emptyList(),
    val components: List<MessageTopLevelComponent> = emptyList(),
    val tts: Boolean = false,
    val original: Message? = null
) : MessagePayload {

    constructor(content: String = "", embed: MessageEmbed): this(content, embeds = listOf(embed))

    fun buildCreate(): MessageCreateData = MessageCreate(this.content, this.embeds, components = this.components, tts = this.tts)

    fun buildEdit(): MessageEditData = MessageEdit(this.content, this.embeds, components = this.components, files = this.files)

}

fun MessagePayload.asDiscordMessageData(): DiscordMessageData =
    when (this) {
        is CoreMessage -> DiscordMessageData(this.content)
        is DiscordMessageData -> this
        else -> error("Unsupported Discord message payload: ${this.javaClass.name}")
    }
