package discord.interact.parse.parsers

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import core.interact.commands.AcceptCommand
import core.interact.commands.Command
import core.session.SessionManager
import discord.interact.UserInteractionContext
import discord.interact.parse.EmbeddableCommand
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

@Suppress("DuplicatedCode")
object AcceptCommandParser : EmbeddableCommand {

    override suspend fun parseComponent(context: UserInteractionContext<GenericComponentInteractionCreateEvent>): Option<Command> {
        val sessionId = SessionManager.findRequestSessionId(context.bot.sessions, context.channel.id, context.user.id)
            ?: return None

        val requestSession = SessionManager.retrieveRequestSession(context.bot.sessions, sessionId).snapshot()
        val accepted = requestSession.opponent.id == context.user.id

        return if (accepted) Some(AcceptCommand(sessionId)) else None
    }

}
