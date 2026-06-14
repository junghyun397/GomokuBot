package discord.interact.parse.parsers

import core.interact.commands.Command
import core.interact.commands.OpeningBranchingCommand
import core.interact.commands.OpeningDeclareCommand
import core.interact.commands.OpeningSwapCommand
import core.session.SessionManager
import core.session.entities.BranchingStageOpeningSession
import core.session.entities.DeclareStageOpeningSession
import core.session.entities.SwapStageOpeningSession
import discord.assets.messageRef
import discord.interact.UserInteractionContext
import discord.interact.parse.EmbeddableCommand
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent

object OpeningCommandParser : EmbeddableCommand {

    override suspend fun parseComponent(context: UserInteractionContext<GenericComponentInteractionCreateEvent>): Command? {
        val sessionId = SessionManager.findGameSessionId(context.bot.sessions, context.channel.id, context.user.id)
            ?: return null
        val session = SessionManager.retrieveGameSession(context.bot.sessions, sessionId).snapshot()

        if (session.player.id != context.user.id) return null

        val id = when (context.event) {
            is StringSelectInteractionEvent -> context.event.interaction.selectedOptions.first().value
            else -> context.event.componentId
        }

        return when (id[2]) {
            's' -> {
                if (session !is SwapStageOpeningSession) return null

                val doSwap = id[3] == 'y'

                OpeningSwapCommand(sessionId, doSwap, context.event.message.messageRef())
            }
            'b' -> {
                if (session !is BranchingStageOpeningSession) return null

                val takeBranch = id[3] == 'y'

                OpeningBranchingCommand(sessionId, takeBranch, context.event.message.messageRef())
            }
            'd' -> {
                if (session !is DeclareStageOpeningSession) return null

                id
                    .drop(3)
                    .toIntOrNull()
                    ?.let { OpeningDeclareCommand(sessionId, it, context.event.message.messageRef()) }
            }
            else -> null
        }
    }

}
