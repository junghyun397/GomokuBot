package discord.interact.parse.parsers

import core.assets.UserId
import core.interact.commands.AcceptCommand
import core.interact.commands.Command
import core.session.SessionManager
import discord.interact.InteractionContext
import discord.interact.parse.EmbeddableCommand
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import utils.structs.Option

@Suppress("DuplicatedCode")
object AcceptCommandParser : EmbeddableCommand {

    override suspend fun parseButton(context: InteractionContext<GenericComponentInteractionCreateEvent>): Option<Command> {
        val owner = context.event.componentId
            .drop(2)
            .toLongOrNull()
            ?.let { UserId(it) }
            ?: return Option.Empty

        val session = SessionManager.retrieveRequestSessionByOwner(context.bot.sessions, context.guild.id, owner)
            ?: return Option.Empty

        return Option.Some(AcceptCommand("accept", session))
    }

}
