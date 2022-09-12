package discord.route

import core.assets.MessageRef
import core.interact.Order
import discord.assets.JDAGuild
import discord.interact.DiscordConfig
import discord.interact.GuildManager
import discord.interact.InteractionContext
import utils.structs.IO

suspend fun export(context: InteractionContext<*>, io: IO<List<Order>>, source: MessageRef?) =
    export(context.discordConfig, context.jdaGuild, io, source)

suspend fun export(discordConfig: DiscordConfig, jdaGuild: JDAGuild, io: IO<List<Order>>, source: MessageRef?) =
    io.run().forEach { order ->
        when (order) {
            is Order.UpsertCommands -> GuildManager.upsertCommands(jdaGuild, order.container)
            is Order.DeleteSource -> source?.also { messageRef ->
                jdaGuild.getTextChannelById(messageRef.channelId.idLong)?.also { channel ->
                    channel.deleteMessageById(source.id.idLong)
                }
            }
            is Order.BulkDelete -> GuildManager.bulkDelete(jdaGuild, order.messageRefs)
            is Order.RemoveNavigators ->
                jdaGuild.getTextChannelById(order.messageRef.channelId.idLong)?.run {
                    GuildManager.retrieveJDAMessage(jdaGuild.jda, order.messageRef)?.let { originalMessage ->
                        GuildManager.clearReaction(originalMessage)

                        if (order.reduceComponents) {
                            GuildManager.retainFirstEmbed(originalMessage)
                            GuildManager.removeComponents(originalMessage)
                        }
                    }
                }
            is Order.ArchiveSession -> GuildManager.archiveSession(
                jdaGuild.jda.getTextChannelById(discordConfig.archiveChannelId.idLong)!!,
                order.session, order.policy
            )
        }
    }
