package discord.route

import core.assets.Order
import core.interact.commands.Command
import discord.interact.GuildManager
import discord.interact.InteractionContext
import discord.interact.command.ParsableCommand
import discord.interact.command.ParseFailure
import discord.interact.message.DiscordMessageBinder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.GuildChannel
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import utils.monads.Either
import utils.monads.IO
import java.util.concurrent.TimeUnit

inline fun withMessagePermissionNode(user: User, guildChannel: GuildChannel, parsableCommand: ParsableCommand, block: (ParsableCommand) -> Either<Command, ParseFailure>) =
    if (GuildManager.lookupPermission(guildChannel, Permission.MESSAGE_SEND)) block(parsableCommand)
    else Either.Right(ParseFailure(parsableCommand.name, "message permission not granted") { container, _ ->
        IO { user.openPrivateChannel()
            .flatMap { channel -> DiscordMessageBinder.sendPermissionNotGrantedEmbed(
                publisher = { msg -> channel.sendMessage(msg) },
                container = container,
                channelName = guildChannel.name
            ) }
            .delay(1, TimeUnit.MINUTES)
            .flatMap(Message::delete)
            .queue()
            Order.UNIT
        }
    })

suspend fun processIO(context: InteractionContext<*>, io: IO<Order>, source: Message?) {
    when (io.run()) {
        Order.REFRESH_COMMANDS -> GuildManager.insertCommands(context.guild, context.config.language.container)
        Order.DELETE_SOURCE -> source?.delete()?.queue()
        Order.UNIT -> Unit
    }
}
