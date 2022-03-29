package discord.interact.command

import core.interact.commands.Command
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import utils.monads.Option

interface EmbeddableCommand {

    fun parse(event: ButtonInteractionEvent): Option<Command>

}