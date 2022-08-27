package discord.route

import core.BotContext
import core.assets.MessageRef
import core.interact.Order
import core.session.SessionManager
import discord.assets.JDAGuild
import discord.interact.DiscordConfig
import discord.interact.GuildManager
import discord.interact.InteractionContext
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction
import utils.structs.IO

suspend fun export(context: InteractionContext<*>, io: IO<List<Order>>, source: MessageRef?) =
    export(context.bot, context.discordConfig, context.jdaGuild, io, source)

suspend fun export(bot: BotContext, discordConfig: DiscordConfig, jdaGuild: JDAGuild, io: IO<List<Order>>, source: MessageRef?) =
    io.run().forEach { order ->
        when (order) {
            is Order.UpsertCommands -> GuildManager.upsertCommands(jdaGuild, order.container)
            is Order.DeleteSource -> source?.also { messageRef ->
                jdaGuild.getTextChannelById(messageRef.channelId.idLong)?.also { channel ->
                    channel.deleteMessageById(source.id.idLong)
                }
            }
            is Order.BulkDelete ->
                SessionManager.checkoutMessages(bot.sessions, order.key)
                    ?.groupBy { it.channelId }
                    ?.forEach { (channelId, messageRefs) -> jdaGuild.getTextChannelById(channelId.idLong)?.let { channel ->
                        try {
                            messageRefs
                                .map { channel.deleteMessageById(it.id.idLong) }
                                .reduce<RestAction<Void>, AuditableRestAction<Void>> { acc, action -> acc.and(action) }
                                .queue()
                        } catch (_: ErrorResponseException) {}
                    } }
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
