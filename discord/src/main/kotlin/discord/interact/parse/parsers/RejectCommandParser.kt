package discord.interact.parse.parsers

import core.interact.commands.Command
import core.interact.commands.RejectCommand
import core.session.SessionManager
import discord.interact.UserInteractionContext
import discord.interact.parse.EmbeddableCommand
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

@Suppress("DuplicatedCode")
object RejectCommandParser : EmbeddableCommand {

    override suspend fun parseComponent(context: UserInteractionContext<GenericComponentInteractionCreateEvent>): Command? {
        val sessionId = SessionManager.findRequestSessionId(context.bot.sessions, context.channel.id, context.user.id)
            ?: return null

        val requestSession = SessionManager.retrieveRequestSession(context.bot.sessions, sessionId).snapshot()
        val accepted = requestSession.opponent.id == context.user.id

        return if (accepted) RejectCommand(sessionId) else null
    }

}
