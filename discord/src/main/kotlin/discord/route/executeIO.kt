package discord.route

import arrow.core.raise.Effect
import arrow.core.raise.get
import core.assets.MessageRef
import core.interact.Order
import discord.assets.JDAChannel
import discord.interact.ChannelManager
import discord.interact.DiscordConfig
import net.dv8tion.jda.api.components.MessageTopLevelComponent

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
                        originalMessage
                            .editMessageComponents(emptyList<MessageTopLevelComponent>())
                            .queue()
                    }
                }
            is Order.ArchiveSession -> ChannelManager.archiveSession(
                jdaChannel.jda.getTextChannelById(discordConfig.archiveSubChannelId.idLong)!!,
                order.session, order.policy
            )
        }
    }
}
