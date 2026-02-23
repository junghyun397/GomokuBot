package discord.interact.parse.parsers

import arrow.core.None
import arrow.core.Some
import arrow.core.toOption
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

    override suspend fun parseComponent(context: UserInteractionContext<GenericComponentInteractionCreateEvent>) =
        SessionManager.retrieveGameSession(context.bot.sessions, context.guild, context.user.id).toOption()
            .flatMap { session ->
                val ref = when (context.config.swapType) {
                    SwapType.EDIT -> SessionManager.viewHeadMessage(context.bot.sessions, session.messageBufferKey)
                    else -> null
                }

                if (session.player.id != context.user.id) return@flatMap None

                val id = when (context.event) {
                    is StringSelectInteractionEvent -> context.event.interaction.selectedOptions.first().value
                    else -> context.event.componentId
                }

                when (id[2]) {
                    's' -> {
                        if (session !is SwapStageOpeningSession) return@flatMap None

                        val doSwap = id[3] == 'y'

                        Some(OpeningSwapCommand(session, doSwap, ref))
                    }
                    'b' -> {
                        if (session !is BranchingStageOpeningSession) return@flatMap None

                        val takeBranch = id[3] == 'y'

                        Some(OpeningBranchingCommand(session, takeBranch, ref))
                    }
                    'd' -> {
                        if (session !is DeclareStageOpeningSession) return@flatMap None

                        id
                            .drop(3)
                            .toIntOrNull()
                            .toOption()
                            .map { OpeningDeclareCommand(session, it, ref) }
                    }
                    else -> None
                }
            }

}
