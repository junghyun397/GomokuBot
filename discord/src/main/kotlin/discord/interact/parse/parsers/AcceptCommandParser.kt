package discord.interact.parse.parsers

import core.assets.UserId
import core.interact.commands.AcceptCommand
import core.interact.commands.Command
import core.session.SessionManager
import discord.interact.InteractionContext
import discord.interact.parse.EmbeddableCommand
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import utils.structs.Option

object AcceptCommandParser : EmbeddableCommand {

    override suspend fun parseButton(context: InteractionContext<ButtonInteractionEvent>): Option<Command> {
        val opponent = UserId(context.event.componentId
            .drop(2)
            .toLongOrNull()
            ?: return Option.Empty
        )

        val session = SessionManager.retrieveRequestSessionByOpponent(context.botContext.sessionRepository, context.guild.id, opponent)
            ?: return Option.Empty

        return Option.Some(AcceptCommand("accept", session))
    }

}
