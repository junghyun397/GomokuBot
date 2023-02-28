package discord.interact.parse

import core.interact.commands.Command
import discord.interact.UserInteractionContext
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import utils.structs.Option

interface EmbeddableCommand {

    suspend fun parseComponent(context: UserInteractionContext<GenericComponentInteractionCreateEvent>): Option<Command>

}
