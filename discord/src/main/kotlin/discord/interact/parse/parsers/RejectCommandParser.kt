package discord.interact.parse.parsers

import core.interact.commands.RejectCommand
import core.session.SessionManager
import discord.interact.UserInteractionContext
import discord.interact.parse.EmbeddableCommand
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import utils.structs.Option

@Suppress("DuplicatedCode")
object RejectCommandParser : EmbeddableCommand {

    override suspend fun parseComponent(context: UserInteractionContext<GenericComponentInteractionCreateEvent>) =
        SessionManager.retrieveRequestSessionByOpponent(context.bot.sessions, context.guild, context.user.id)
            ?.let { Option.Some(RejectCommand(it)) }
            ?: Option.Empty

}
