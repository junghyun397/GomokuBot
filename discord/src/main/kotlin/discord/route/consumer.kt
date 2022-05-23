package discord.route

import core.BotContext
import core.interact.Order
import core.session.SessionManager
import discord.assets.JDAGuild
import discord.interact.GuildManager
import discord.interact.InteractionContext
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import utils.structs.IO

suspend fun consumeIO(context: InteractionContext<*>, io: IO<Order>, source: Message?) =
    consumeIO(context.bot, context.jdaGuild, io, source)

suspend fun consumeIO(bot: BotContext, jdaGuild: JDAGuild, io: IO<Order>, source: Message?) =
    when (val order = io.run()) {
        is Order.UpsertCommands -> GuildManager.upsertCommands(jdaGuild, order.container)
        is Order.DeleteSource -> source?.delete()?.queue()
        is Order.BulkDelete ->
            SessionManager.checkoutMessages(bot.sessionRepository, order.key)
                ?.groupBy { it.channelId }
                ?.forEach { entry -> jdaGuild.getTextChannelById(entry.key.idLong)?.let { channel ->
                    try {
                        GuildManager.permissionSafeRun(channel, Permission.MESSAGE_MANAGE) { _ ->
                            if (entry.value.size < 2)
                                channel.deleteMessageById(entry.value.first().id.idLong.toString()).queue()
                            else
                                channel.deleteMessagesByIds(entry.value.map { it.id.idLong.toString() }).queue()
                        }
                    } catch (_: ErrorResponseException) { }
                } }
        is Order.RemoveNavigators -> GuildManager.retrieveJDAMessage(jdaGuild.jda, order.message)
            ?.let {
                if (order.retainFistEmbed)
                    GuildManager.retainFirstEmbed(it)

                GuildManager.removeNavigators(it)
            }
        is Order.ArchiveSession -> GuildManager.archiveSession(
            jdaGuild.jda.getTextChannelById(553959991489331200)!!,
            order.session, order.policy
        )
        is Order.Unit -> Unit
    }
