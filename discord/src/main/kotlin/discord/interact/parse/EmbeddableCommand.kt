package discord.interact.parse

import core.interact.commands.Command
import discord.interact.InteractionContext
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import utils.structs.Option

interface EmbeddableCommand {

    suspend fun parseButton(context: InteractionContext<GenericComponentInteractionCreateEvent>): Option<Command>

}
