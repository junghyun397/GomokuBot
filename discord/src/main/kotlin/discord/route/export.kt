package discord.route

import core.assets.Guild
import core.assets.MessageRef
import core.interact.Order
import discord.assets.JDAGuild
import discord.assets.extractMessageData
import discord.interact.DiscordConfig
import discord.interact.GuildManager
import utils.structs.IO

suspend fun export(discordConfig: DiscordConfig, io: IO<List<Order>>, guild: Guild, jdaGuild: JDAGuild, source: MessageRef? = null) {
    io.run().forEach { order ->
        when (order) {
            is Order.UpsertCommands -> GuildManager.upsertCommands(guild.id, jdaGuild, order.container)
            is Order.DeleteSource -> source?.let { GuildManager.deleteSingle(jdaGuild, it) }
            is Order.BulkDelete -> GuildManager.bulkDelete(jdaGuild, order.messageRefs)
            is Order.RemoveNavigators -> GuildManager.retrieveJDAMessage(jdaGuild.jda, order.messageRef)
                ?.let { originalMessage ->
                    GuildManager.clearReaction(originalMessage)

                    if (order.reduceComponents) {
                        originalMessage.extractMessageData()
                            .let { GuildManager.retainFirstEmbed(it) }
                            .let { GuildManager.clearComponents(it) }
                            .let { messageData ->
                                originalMessage.editMessage(messageData.buildEdit()).queue()
                            }
                    }
                }

            is Order.ArchiveSession -> GuildManager.archiveSession(
                jdaGuild.jda.getTextChannelById(discordConfig.archiveChannelId.idLong)!!,
                order.session, order.policy
            )
        }
    }
}
