package discord.interact.parse.parsers

import core.interact.commands.ApplySettingCommand
import core.interact.commands.Command
import core.session.ArchivePolicy
import core.session.BoardStyle
import core.session.FocusPolicy
import core.session.SweepPolicy
import discord.interact.InteractionContext
import discord.interact.parse.EmbeddableCommand
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import utils.lang.and
import utils.structs.Option
import utils.structs.asOption
import utils.structs.map

object ApplySettingCommandParser : EmbeddableCommand {

    override suspend fun parseButton(context: InteractionContext<GenericComponentInteractionCreateEvent>): Option<Command> {
        val (kind, choice) = when(context.event) {
            is SelectMenuInteractionEvent -> context.event.interaction.selectedOptions.first().value.split("-")
            else -> context.event.componentId.split("-").drop(1)
        }

        return runCatching { when (kind) {
            BoardStyle::class.simpleName -> {
                val style = BoardStyle.valueOf(choice)

                style and context.config.copy(boardStyle = style)
            }
            FocusPolicy::class.simpleName -> {
                val focus = FocusPolicy.valueOf(choice)

                focus and context.config.copy(focusPolicy = focus)
            }
            SweepPolicy::class.simpleName -> {
                val sweep = SweepPolicy.valueOf(choice)
                sweep and context.config.copy(sweepPolicy = sweep)
            }
            ArchivePolicy::class.simpleName -> {
                val archive = ArchivePolicy.valueOf(choice)
                archive and context.config.copy(archivePolicy = archive)
            }
            else -> throw IllegalStateException()
        } }
            .asOption()
            .map { (diff, newConfig) -> ApplySettingCommand(newConfig, diff) }
    }

}
