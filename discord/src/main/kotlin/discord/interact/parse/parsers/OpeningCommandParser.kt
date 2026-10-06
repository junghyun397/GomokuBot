package discord.interact.parse.parsers

import core.database.DatabaseConnection
import core.interact.commands.Command
import core.interact.commands.OpeningBranchingCommand
import core.interact.commands.OpeningDeclareCommand
import core.interact.commands.OpeningSwapCommand
import core.session.SessionManager
import core.session.SessionPool
import core.session.entities.BranchingStageOpeningSession
import core.session.entities.DeclareStageOpeningSession
import core.session.entities.SwapStageOpeningSession
import discord.interact.UserInteractionContext
import discord.interact.parse.EmbeddableCommand
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent

object OpeningCommandParser : EmbeddableCommand {

    context(dbConnection: DatabaseConnection, sessions: SessionPool)
    override suspend fun parseComponent(context: UserInteractionContext<GenericComponentInteractionCreateEvent>): Command? {
        val sessionId = SessionManager.findGameSessionId(context.channel.id, context.user.id)
            ?: return null
        val session = SessionManager.retrieveGameSession(sessionId).snapshot()

        if (session.player.id != context.user.id) return null

        val (_, option) = when (context.event) {
            is StringSelectInteractionEvent -> context.event.interaction.selectedOptions.first().value
            else -> context.event.componentId
        }.split("-")

        return when (option.first()) {
            's' -> {
                if (session !is SwapStageOpeningSession) return null

                val doSwap = option[1] == 'y'

                OpeningSwapCommand(sessionId, doSwap)
            }
            'b' -> {
                if (session !is BranchingStageOpeningSession) return null

                val takeBranch = option[1] == 'y'

                OpeningBranchingCommand(sessionId, takeBranch)
            }
            'd' -> {
                if (session !is DeclareStageOpeningSession) return null

                option
                    .drop(1)
                    .toIntOrNull()
                    ?.let { OpeningDeclareCommand(sessionId, it) }
            }
            else -> null
        }
    }

}
