package discord.route

import arrow.core.raise.Effect
import arrow.core.raise.get
import core.assets.MessageRef
import core.interact.Order
import discord.assets.JDAChannel
import discord.assets.extractMessageData
import discord.interact.ChannelManager
import discord.interact.ChannelManager.clearComponents
import discord.interact.ChannelManager.retainFirstEmbed
import discord.interact.DiscordConfig

suspend fun executeIO(discordConfig: DiscordConfig, io: Effect<Nothing, List<Order>>, jdaChannel: JDAChannel, source: MessageRef? = null) {
    io.get().forEach { order ->
        when (order) {
            is Order.UpsertCommands -> ChannelManager.upsertCommands(jdaChannel, order.container)
            is Order.DeleteSource -> source?.let { ChannelManager.deleteSingle(jdaChannel, it) }
            is Order.BulkDelete -> ChannelManager.bulkDelete(jdaChannel, order.messageRefs)
            is Order.RemoveNavigators -> ChannelManager.retrieveJDAMessage(jdaChannel.jda, order.messageRef)
                ?.let { originalMessage ->
                    ChannelManager.clearReaction(originalMessage)

                    if (order.reduceComponents) {
                        val messageData = originalMessage.extractMessageData()
                            .retainFirstEmbed()
                            .clearComponents()

                        originalMessage.editMessage(messageData.buildEdit()).queue()
                    }
                }
            is Order.ArchiveSession -> ChannelManager.archiveSession(
                jdaChannel.jda.getTextChannelById(discordConfig.archiveSubChannelId.idLong)!!,
                order.session, order.policy
            )
        }
    }
}
