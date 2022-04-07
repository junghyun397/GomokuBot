package discord.route

import core.interact.Order
import discord.assets.extractUser
import discord.interact.GuildManager
import discord.interact.InteractionContext
import discord.interact.message.DiscordMessageProducer
import discord.interact.parse.DiscordParseFailure
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import utils.structs.IO
import utils.structs.Option
import java.util.concurrent.TimeUnit

suspend fun processIO(context: InteractionContext<*>, io: IO<Order>, source: Message?) =
    when (val order = io.run()) {
        is Order.RefreshCommands -> GuildManager.insertCommands(context.jdaGuild, context.config.language.container)
        is Order.DeleteSource -> source?.delete()?.queue()
        is Order.BulkDelete -> {
            order.messages
                .groupBy { it.channelId }
                .forEach { entry -> context.jdaGuild.getTextChannelById(entry.key.idLong)?.let { channel ->
                    GuildManager.permissionSafeRun(channel, Permission.MESSAGE_MANAGE) { _ ->
                        channel.deleteMessagesByIds(entry.value.map { it.id.toString() })
                    }
                } }
        }
        is Order.ArchiveSession -> GuildManager.archiveGame(context.archiveChannel, order.session, order.policy)
        is Order.Unit -> Unit
    }

fun mergeMessagePermission(channel: TextChannel, user: User, parsableCommand: ParsableCommand): Option<DiscordParseFailure> =
    GuildManager.permissionNotGrantedRun(channel, Permission.MESSAGE_SEND) {
        DiscordParseFailure(parsableCommand.name, "message permission not granted", user.extractUser()) { _, _, container ->
            IO { user.openPrivateChannel()
                .flatMap { privateChannel -> DiscordMessageProducer.sendPermissionNotGrantedEmbed(
                    container = container,
                    channelName = channel.name
                ) { msg -> privateChannel.sendMessage(msg) } }
                .delay(1, TimeUnit.MINUTES)
                .flatMap(Message::delete)
                .queue()
                Order.Unit
            }
        }
    }
