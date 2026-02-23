package discord.interact.parse.parsers

import arrow.core.None
import arrow.core.Some
import core.interact.commands.RejectCommand
import core.session.SessionManager
import discord.interact.UserInteractionContext
import discord.interact.parse.EmbeddableCommand
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

@Suppress("DuplicatedCode")
object RejectCommandParser : EmbeddableCommand {

    override suspend fun parseComponent(context: UserInteractionContext<GenericComponentInteractionCreateEvent>) =
        SessionManager.retrieveRequestSessionByOpponent(context.bot.sessions, context.guild, context.user.id)
            ?.let { Some(RejectCommand(it)) }
            ?: None

}
