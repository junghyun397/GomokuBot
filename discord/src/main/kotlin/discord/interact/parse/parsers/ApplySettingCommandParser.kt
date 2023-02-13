package discord.interact.parse.parsers

import core.interact.commands.ApplySettingCommand
import core.interact.commands.Command
import core.interact.message.SettingMapping
import discord.interact.InteractionContext
import discord.interact.parse.EmbeddableCommand
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import utils.structs.Option
import utils.structs.map

object ApplySettingCommandParser : EmbeddableCommand {

    override suspend fun parseComponent(context: InteractionContext<GenericComponentInteractionCreateEvent>): Option<Command> {
        val (kind, choice) = when(context.event) {
            is StringSelectInteractionEvent -> context.event.interaction.selectedOptions.first().value.split("-")
            else -> context.event.componentId.split("-").drop(1)
        }

        return SettingMapping.buildDifference(context.config, kind, choice)
            .map { (diff, newConfig) -> ApplySettingCommand(newConfig, diff) }
    }

}
