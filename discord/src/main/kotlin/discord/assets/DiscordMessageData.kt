package discord.assets

import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.utils.AttachedFile
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditData
import utils.structs.Option

data class DiscordMessageData(
    val content: String = "",
    val embeds: List<MessageEmbed> = emptyList(),
    val files: List<AttachedFile> = emptyList(),
    val components: List<LayoutComponent> = emptyList(),
    val tts: Boolean = false,
    val original: Option<Message> = Option.Empty
) {

    constructor(content: String = "", embed: MessageEmbed): this(content, embeds = listOf(embed))

    fun buildCreate(): MessageCreateData = MessageCreate(this.content, this.embeds, tts = this.tts)

    fun buildEdit(): MessageEditData = MessageEdit(this.content, this.embeds)

}
