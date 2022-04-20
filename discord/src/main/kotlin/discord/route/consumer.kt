package discord.route

import core.interact.Order
import core.session.SessionManager
import discord.interact.GuildManager
import discord.interact.InteractionContext
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import utils.structs.IO

suspend fun consumeIO(context: InteractionContext<*>, io: IO<Order>, source: Message?) =
    when (val order = io.run()) {
        is Order.RefreshCommands -> GuildManager.insertCommands(context.jdaGuild, order.container)
        is Order.DeleteSource -> source?.delete()?.queue()
        is Order.BulkDelete ->
            SessionManager.checkoutMessages(context.botContext.sessionRepository, order.key)
                ?.groupBy { it.channelId }
                ?.forEach { entry -> context.jdaGuild.getTextChannelById(entry.key.idLong)?.let { channel ->
                    GuildManager.permissionSafeRun(channel, Permission.MESSAGE_MANAGE) { _ ->
                        if (entry.value.size < 2)
                            channel.deleteMessageById(entry.value.first().id.idLong.toString()).queue()
                        else
                            channel.deleteMessagesByIds(entry.value.map { it.id.idLong.toString() }).queue()
                    }
                } }
        is Order.ArchiveSession -> GuildManager.archiveSession(context.archiveChannel, order.session, order.policy)
        is Order.Unit -> Unit
    }
