package discord.interact.parse.parsers

import core.interact.commands.Command
import core.interact.commands.ConfigCommand
import core.session.ArchivePolicy
import core.session.FocusPolicy
import core.session.SweepPolicy
import discord.interact.InteractionContext
import discord.interact.parse.EmbeddableCommand
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import utils.structs.Option

object ConfigCommandParser : EmbeddableCommand {

    override suspend fun parseButton(context: InteractionContext<GenericComponentInteractionCreateEvent>): Option<Command> {
        val (kind, choice) = if (context.event !is SelectMenuInteractionEvent)
            context.event.componentId.split("-").drop(1)
        else context.event.interaction.selectedOptions.first().value.split("-")

        val (newConfig, kindName, choiceName) = when (kind) {
            "f" -> when (choice) {
                "i" -> Triple(
                    context.config.copy(focusPolicy = FocusPolicy.INTELLIGENCE),
                    0,
                    context.config.language.container.focusSelectIntelligence()
                )
                "f" -> Triple(
                    context.config.copy(focusPolicy = FocusPolicy.FALLOWING),
                    0,
                    context.config.language.container.focusSelectFallowing()
                )
                else -> throw IllegalStateException()
            }
            "s" -> when (choice) {
                "r" -> Triple(
                    context.config.copy(sweepPolicy = SweepPolicy.RELAY),
                    0,
                    context.config.language.container.sweepSelectRelay()
                )
                "l" -> Triple(
                    context.config.copy(sweepPolicy = SweepPolicy.LEAVE),
                    0,
                    context.config.language.container.sweepSelectLeave()
                )
                else -> throw IllegalStateException()
            }
            "a" -> when (choice) {
                "a" -> Triple(
                    context.config.copy(archivePolicy = ArchivePolicy.BY_ANONYMOUS),
                    0,
                    context.config.language.container.archiveSelectByAnonymous()
                )
                "p" -> Triple(
                    context.config.copy(archivePolicy = ArchivePolicy.WITH_PROFILE),
                    0,
                    context.config.language.container.archiveSelectWithProfile()
                )
                "b" -> Triple(
                    context.config.copy(archivePolicy = ArchivePolicy.PRIVACY),
                    0,
                    context.config.language.container.archiveSelectPrivacy()
                )
                else -> throw IllegalStateException()
            }
            else -> throw IllegalStateException()
        }

        return Option(ConfigCommand("p", newConfig, kindName.toString(), choiceName))
    }

}
