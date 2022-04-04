package discord.route

import core.interact.Order
import core.interact.commands.Command
import discord.interact.GuildManager
import discord.interact.InteractionContext
import discord.interact.message.DiscordMessageProducer
import discord.interact.parse.DiscordParseFailure
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import utils.structs.Either
import utils.structs.IO
import java.util.concurrent.TimeUnit

inline fun withMessagePermissionNode(channel: TextChannel, user: User, parsableCommand: ParsableCommand, block: (ParsableCommand) -> Either<Command, DiscordParseFailure>) =
    if (GuildManager.lookupPermission(channel, Permission.MESSAGE_SEND)) block(parsableCommand)
    else Either.Right(DiscordParseFailure(parsableCommand.name, "message permission not granted") { _, _, container ->
        IO { user.openPrivateChannel()
            .flatMap { channel -> DiscordMessageProducer.sendPermissionNotGrantedEmbed(
                publisher = { msg -> channel.sendMessage(msg) },
                container = container,
                channelName = channel.name
            ) }
            .delay(1, TimeUnit.MINUTES)
            .flatMap(Message::delete)
            .queue()
            Order.Unit
        }
    })

suspend fun processIO(context: InteractionContext<*>, io: IO<Order>, source: Message?) {
    when (val order = io.run()) {
        is Order.RefreshCommands -> GuildManager.insertCommands(context.jdaGuild, context.config.language.container)
        is Order.DeleteSource -> source?.delete()?.queue()
        is Order.BulkDelete -> {
            order.messages
                .groupBy { it.channelId }
                .forEach { entry -> context.jdaGuild.getTextChannelById(entry.key.id)?.let { channel ->
                    GuildManager.permissionSafeRun(channel, Permission.MESSAGE_MANAGE) { _ ->
                        channel.deleteMessagesByIds(entry.value.map { it.id.toString() })
                    }
                } }
        }
        is Order.ArchiveSession -> GuildManager.archiveGame(order.session, order.policy)
        is Order.Unit -> Unit
    }
}
