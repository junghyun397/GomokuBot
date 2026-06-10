package discord.interact.parse.parsers

import core.session.MessageManager
import core.assets.humanId
import core.interact.commands.Command
import core.interact.commands.OpeningBranchingCommand
import core.interact.commands.OpeningDeclareCommand
import core.interact.commands.OpeningSwapCommand
import core.session.SessionManager
import core.session.SwapType
import core.session.entities.BranchingStageOpeningSession
import core.session.entities.DeclareStageOpeningSession
import core.session.entities.SwapStageOpeningSession
import discord.interact.UserInteractionContext
import discord.interact.parse.EmbeddableCommand
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent

object OpeningCommandParser : EmbeddableCommand {

    override suspend fun parseComponent(context: UserInteractionContext<GenericComponentInteractionCreateEvent>): Command? {
        val sessionId = SessionManager.findGameSessionId(context.bot.sessions, context.channel.id, context.user.id)
            ?: return null
        val session = SessionManager.retrieveGameSession(context.bot.sessions, sessionId).snapshot()
        val ref = when (context.config.swapType) {
            SwapType.EDIT -> MessageManager.viewHeadMessage(context.bot.sessions, session.messageBufferKey)
            else -> null
        }

        if (session.player.humanId != context.user.id) return null

        val id = when (context.event) {
            is StringSelectInteractionEvent -> context.event.interaction.selectedOptions.first().value
            else -> context.event.componentId
        }

        return when (id[2]) {
            's' -> {
                if (session !is SwapStageOpeningSession) return null

                val doSwap = id[3] == 'y'

                OpeningSwapCommand(sessionId, doSwap, ref)
            }
            'b' -> {
                if (session !is BranchingStageOpeningSession) return null

                val takeBranch = id[3] == 'y'

                OpeningBranchingCommand(sessionId, takeBranch, ref)
            }
            'd' -> {
                if (session !is DeclareStageOpeningSession) return null

                id
                    .drop(3)
                    .toIntOrNull()
                    ?.let { OpeningDeclareCommand(sessionId, it, ref) }
            }
            else -> null
        }
    }

}
