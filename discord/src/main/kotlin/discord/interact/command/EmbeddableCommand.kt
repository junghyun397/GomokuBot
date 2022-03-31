package discord.interact.command

import core.interact.commands.Command
import discord.interact.InteractionContext
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import utils.monads.Option

interface EmbeddableCommand {

    suspend fun parseButton(context: InteractionContext<ButtonInteractionEvent>): Option<Command>

}
