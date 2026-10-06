package discord.interact.message

import core.assets.MessageRef
import core.interact.message.SentMessage
import discord.assets.messageRef
import net.dv8tion.jda.api.entities.Message

class DiscordSentMessage(internal val original: Message) : SentMessage {

    override val ref: MessageRef by lazy { this.original.messageRef() }


}
