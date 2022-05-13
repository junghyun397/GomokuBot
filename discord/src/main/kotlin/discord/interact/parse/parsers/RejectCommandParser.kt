package discord.interact.parse.parsers

import core.assets.UserId
import core.interact.commands.Command
import core.interact.commands.RejectCommand
import core.session.SessionManager
import discord.assets.extractId
import discord.interact.InteractionContext
import discord.interact.parse.EmbeddableCommand
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import utils.structs.Option

@Suppress("DuplicatedCode")
object RejectCommandParser : EmbeddableCommand {

    override suspend fun parseButton(context: InteractionContext<ButtonInteractionEvent>): Option<Command> {
        val owner = context.event.componentId
            .drop(2)
            .toLongOrNull()
            ?.let { UserId(it) }
            ?: return Option.Empty

        if (context.event.user.extractId() == owner)
            return Option.Empty

        val session = SessionManager.retrieveRequestSessionByOwner(context.bot.sessionRepository, context.guild.id, owner)
            ?: return Option.Empty

        return Option.Some(RejectCommand("reject", session))
    }

}
