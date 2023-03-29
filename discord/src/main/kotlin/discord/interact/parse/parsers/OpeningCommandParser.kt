package discord.interact.parse.parsers

import core.interact.commands.OpeningBranchingCommand
import core.interact.commands.OpeningSwapCommand
import core.session.SessionManager
import core.session.SwapType
import core.session.entities.BranchingStageOpeningSession
import core.session.entities.SwapStageOpeningSession
import discord.interact.UserInteractionContext
import discord.interact.parse.EmbeddableCommand
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import utils.structs.Option
import utils.structs.asOption
import utils.structs.flatMap

object OpeningCommandParser : EmbeddableCommand {

    override suspend fun parseComponent(context: UserInteractionContext<GenericComponentInteractionCreateEvent>) =
        SessionManager.retrieveGameSession(context.bot.sessions, context.guild, context.user.id).asOption()
            .flatMap { session ->
                val ref = when (context.config.swapType) {
                    SwapType.EDIT -> SessionManager.viewHeadMessage(context.bot.sessions, session.messageBufferKey)
                    else -> null
                }

                if (session.player.id != context.user.id) return@flatMap Option.Empty

                when (context.event.componentId[2]) {
                    's' -> {
                        if (session !is SwapStageOpeningSession) return@flatMap Option.Empty

                        val doSwap = context.event.componentId[3] == 'y'

                        Option.Some(OpeningSwapCommand(session, doSwap, ref))
                    }
                    'b' -> {
                        if (session !is BranchingStageOpeningSession) return@flatMap Option.Empty

                        val takeBranch = context.event.componentId[3] == 'y'

                        Option.Some(OpeningBranchingCommand(session, takeBranch, ref))
                    }
                    else -> Option.Empty
                }
            }

}
