package discord.interact.parse.parsers

import core.interact.commands.ApplySettingCommand
import core.interact.commands.Command
import core.session.*
import discord.interact.InteractionContext
import discord.interact.parse.EmbeddableCommand
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import utils.lang.pair
import utils.structs.Option
import utils.structs.map
import utils.structs.toOption

object ApplySettingCommandParser : EmbeddableCommand {

    override suspend fun parseButton(context: InteractionContext<GenericComponentInteractionCreateEvent>): Option<Command> {
        val (kind, choice) = when(context.event) {
            is SelectMenuInteractionEvent -> context.event.interaction.selectedOptions.first().value.split("-")
            else -> context.event.componentId.split("-").drop(1)
        }

        return runCatching { when (kind) {
            BoardStyle::class.simpleName -> {
                val style = BoardStyle.valueOf(choice)

                style pair context.config.copy(boardStyle = style)
            }
            FocusPolicy::class.simpleName -> {
                val focus = FocusPolicy.valueOf(choice)

                focus pair context.config.copy(focusPolicy = focus)
            }
            HintPolicy::class.simpleName -> {
                val hint = HintPolicy.valueOf(choice)

                hint pair context.config.copy(hintPolicy = hint)
            }
            SweepPolicy::class.simpleName -> {
                val sweep = SweepPolicy.valueOf(choice)
                sweep pair context.config.copy(sweepPolicy = sweep)
            }
            ArchivePolicy::class.simpleName -> {
                val archive = ArchivePolicy.valueOf(choice)
                archive pair context.config.copy(archivePolicy = archive)
            }
            else -> throw IllegalStateException()
        } }
            .toOption()
            .map { (diff, newConfig) -> ApplySettingCommand(newConfig, diff) }
    }

}
