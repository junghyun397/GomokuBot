package discord.interact.message

import core.assets.MessageRef
import core.interact.message.MessageAdaptor
import discord.assets.extractMessageData
import discord.assets.extractMessageRef
import net.dv8tion.jda.api.entities.Message

class DiscordMessageAdaptor(private val original: Message) : MessageAdaptor<DiscordMessageData, DiscordComponents> {

    override val messageRef: MessageRef by lazy { this.original.extractMessageRef() }

    override val messageData: DiscordMessageData get() = this.original.extractMessageData()

}
