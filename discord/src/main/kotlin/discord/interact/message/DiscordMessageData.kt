package discord.interact.message

import net.dv8tion.jda.api.components.MessageTopLevelComponent
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder
import net.dv8tion.jda.api.utils.messages.MessageEditData

data class DiscordMessageData(
    val content: String = "",
    val embeds: List<MessageEmbed> = emptyList(),
    val files: List<FileUpload> = emptyList(),
    val components: List<MessageTopLevelComponent> = emptyList(),
) {

    constructor(embed: MessageEmbed) : this(embeds = listOf(embed))

    fun buildCreate(): MessageCreateData = MessageCreateBuilder()
        .setContent(this.content)
        .setEmbeds(this.embeds)
        .setComponents(this.components)
        .setFiles(this.files)
        .build()

    fun buildEdit(): MessageEditData = MessageEditBuilder()
        .setReplace(true)
        .setContent(this.content)
        .setEmbeds(this.embeds)
        .setComponents(this.components)
        .setAttachments(this.files)
        .build()

}
