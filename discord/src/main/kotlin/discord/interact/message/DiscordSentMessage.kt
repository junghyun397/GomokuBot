package discord.interact.message

import core.assets.MessageRef
import core.interact.message.SentMessage
import discord.assets.extractMessageData
import discord.assets.extractMessageRef
import net.dv8tion.jda.api.entities.Message

class DiscordSentMessage(private val original: Message) : SentMessage {

    override val ref: MessageRef by lazy { this.original.extractMessageRef() }

    val messageData: DiscordMessageData get() = this.original.extractMessageData()

}
