package discord.route

import core.BotContext
import core.interact.Order
import core.session.SessionManager
import discord.assets.JDAGuild
import discord.interact.DiscordConfig
import discord.interact.GuildManager
import discord.interact.InteractionContext
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import utils.structs.IO

suspend fun export(context: InteractionContext<*>, io: IO<List<Order>>, source: Message?) =
    export(context.bot, context.discordConfig, context.jdaGuild, io, source)

suspend fun export(bot: BotContext, discordConfig: DiscordConfig, jdaGuild: JDAGuild, io: IO<List<Order>>, source: Message?) =
    io.run().forEach { order ->
        when (order) {
            is Order.UpsertCommands -> GuildManager.upsertCommands(jdaGuild, order.container)
            is Order.DeleteSource -> source?.delete()?.queue()
            is Order.BulkDelete ->
                SessionManager.checkoutMessages(bot.sessions, order.key)
                    ?.groupBy { it.channelId }
                    ?.forEach { (key, value) -> jdaGuild.getTextChannelById(key.idLong)?.let { channel ->
                        try {
                            GuildManager.permissionGrantedRun(channel, Permission.MESSAGE_MANAGE) {
                                if (value.size < 2)
                                    channel.deleteMessageById(value.first().id.idLong.toString()).queue()
                                else
                                    channel.deleteMessagesByIds(value.map { it.id.idLong.toString() }).queue()
                            }
                        } catch (_: ErrorResponseException) {}
                    } }
            is Order.RemoveNavigators -> GuildManager.retrieveJDAMessage(jdaGuild.jda, order.messageRef)
                ?.let {
                    if (order.reduceComponents) {
                        GuildManager.retainFirstEmbed(it)
                        GuildManager.removeComponents(it)
                    }

                    GuildManager.removeReactions(it)
                }
            is Order.ArchiveSession -> GuildManager.archiveSession(
                jdaGuild.jda.getTextChannelById(discordConfig.archiveChannelId.idLong)!!,
                order.session, order.policy
            )
        }
    }
