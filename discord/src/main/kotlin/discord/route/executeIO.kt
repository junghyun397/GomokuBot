package discord.route

import arrow.core.raise.Effect
import arrow.core.raise.get
import core.assets.MessageRef
import core.interact.Order
import discord.assets.JDAGuild
import discord.assets.extractMessageData
import discord.interact.DiscordConfig
import discord.interact.GuildManager
import discord.interact.GuildManager.clearComponents
import discord.interact.GuildManager.retainFirstEmbed

suspend fun executeIO(discordConfig: DiscordConfig, io: Effect<Nothing, List<Order>>, jdaGuild: JDAGuild, source: MessageRef? = null) {
    io.get().forEach { order ->
        when (order) {
            is Order.UpsertCommands -> GuildManager.upsertCommands(jdaGuild, order.container)
            is Order.DeleteSource -> source?.let { GuildManager.deleteSingle(jdaGuild, it) }
            is Order.BulkDelete -> GuildManager.bulkDelete(jdaGuild, order.messageRefs)
            is Order.RemoveNavigators -> GuildManager.retrieveJDAMessage(jdaGuild.jda, order.messageRef)
                ?.let { originalMessage ->
                    GuildManager.clearReaction(originalMessage)

                    if (order.reduceComponents) {
                        val messageData = originalMessage.extractMessageData()
                            .retainFirstEmbed()
                            .clearComponents()

                        originalMessage.editMessage(messageData.buildEdit()).queue()
                    }
                }
            is Order.ArchiveSession -> GuildManager.archiveSession(
                jdaGuild.jda.getTextChannelById(discordConfig.archiveChannelId.idLong)!!,
                order.session, order.policy
            )
        }
    }
}
