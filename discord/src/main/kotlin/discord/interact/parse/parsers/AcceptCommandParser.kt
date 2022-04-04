package discord.interact.parse.parsers

import core.interact.commands.Command
import discord.interact.InteractionContext
import discord.interact.parse.EmbeddableCommand
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import utils.structs.Option

object AcceptCommandParser : EmbeddableCommand {

    override suspend fun parseButton(context: InteractionContext<ButtonInteractionEvent>): Option<Command> {
        TODO("Not yet implemented")
    }

}
