package discord.interact.parse.parsers

import core.assets.UserId
import core.database.DatabaseManager
import core.interact.commands.Command
import core.interact.commands.RejectCommand
import core.session.SessionManager
import discord.assets.DISCORD_PLATFORM_ID
import discord.interact.InteractionContext
import discord.interact.parse.EmbeddableCommand
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import utils.structs.Option

@Suppress("DuplicatedCode")
object RejectCommandParser : EmbeddableCommand {

    override suspend fun parseButton(context: InteractionContext<GenericComponentInteractionCreateEvent>): Option<Command> {
        val owner = context.event.componentId
            .drop(2)
            .toLongOrNull()
            ?.let { DatabaseManager.retrieveUser(context.bot.databaseConnection, DISCORD_PLATFORM_ID, UserId(it)) }
            ?.getOrException()
            ?: return Option.Empty

        val session = SessionManager.retrieveRequestSessionByOwner(context.bot.sessions, context.guild, owner.id)
            ?: return Option.Empty

        return Option(RejectCommand("reject", session))
    }

}
