package discord.interact.parse.parsers

import core.interact.commands.AcceptCommand
import core.session.SessionManager
import discord.interact.InteractionContext
import discord.interact.parse.EmbeddableCommand
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import utils.structs.Option

@Suppress("DuplicatedCode")
object AcceptCommandParser : EmbeddableCommand {

    override suspend fun parseButton(context: InteractionContext<GenericComponentInteractionCreateEvent>) =
        SessionManager.retrieveRequestSessionByOpponent(context.bot.sessions, context.guild, context.user.id)
            ?.let { Option.Some(AcceptCommand("accept", it)) }
            ?: Option.Empty

}
