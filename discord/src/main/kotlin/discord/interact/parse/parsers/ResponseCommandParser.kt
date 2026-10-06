package discord.interact.parse.parsers

import core.database.DatabaseConnection
import core.interact.commands.Command
import core.interact.commands.ResponseCommand
import core.session.SessionManager
import core.session.SessionPool
import core.session.entities.SessionId
import discord.interact.UserInteractionContext
import discord.interact.message.DiscordComponentIds
import discord.interact.parse.EmbeddableCommand
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import java.util.*

object ResponseCommandParser : EmbeddableCommand {

    context(dbConnection: DatabaseConnection, sessions: SessionPool)
    override suspend fun parseComponent(context: UserInteractionContext<GenericComponentInteractionCreateEvent>): Command? {
        val componentId = context.event.componentId
        val accept = when (componentId.firstOrNull()) {
            DiscordComponentIds.ACCEPT -> true
            DiscordComponentIds.REJECT -> false
            else -> return null
        }

        val requestId = runCatching { SessionId(UUID.fromString(componentId.substringAfter('-'))) }.getOrNull()
            ?: return null
        val currentId = SessionManager.findRequestSessionId(context.channel.id, context.user.id)
        if (requestId != currentId) return null

        val requestSession = SessionManager.retrieveRequestSession(requestId).snapshot()
        if (requestSession.recipient.id != context.user.id)
            return null

        return ResponseCommand(requestId, accept)
    }

}
