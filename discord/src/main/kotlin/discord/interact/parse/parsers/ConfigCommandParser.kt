package discord.interact.parse.parsers

import core.interact.commands.Command
import core.interact.commands.ConfigCommand
import core.interact.message.graphics.BoardStyle
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
        val (kind, choice) = run {
            if (context.event !is SelectMenuInteractionEvent)
                context.event.componentId.split("-").drop(1)
            else
                context.event.interaction.selectedOptions.first().value.split("-")
        }

        val (newConfig, kindName, choiceName) = runCatching { when (kind) {
            BoardStyle::class.simpleName -> when (BoardStyle.valueOf(choice)) {
                BoardStyle.IMAGE -> Triple(
                    context.config.copy(boardStyle = BoardStyle.IMAGE),
                    0,
                    context.config.language.container.styleSelectImage()
                )
                BoardStyle.TEXT -> Triple(
                    context.config.copy(boardStyle = BoardStyle.TEXT),
                    0,
                    context.config.language.container.styleSelectText()
                )
                BoardStyle.SOLID_TEXT -> Triple(
                    context.config.copy(boardStyle = BoardStyle.SOLID_TEXT),
                    0,
                    context.config.language.container.styleSelectSolidText()
                )
                BoardStyle.UNICODE -> Triple(
                    context.config.copy(boardStyle = BoardStyle.UNICODE),
                    0,
                    context.config.language.container.styleSelectUnicodeText()
                )
            }
            FocusPolicy::class.simpleName -> when (FocusPolicy.valueOf(choice)) {
                FocusPolicy.INTELLIGENCE -> Triple(
                    context.config.copy(focusPolicy = FocusPolicy.INTELLIGENCE),
                    0,
                    context.config.language.container.focusSelectIntelligence()
                )
                FocusPolicy.FALLOWING -> Triple(
                    context.config.copy(focusPolicy = FocusPolicy.FALLOWING),
                    0,
                    context.config.language.container.focusSelectFallowing()
                )
            }
            SweepPolicy::class.simpleName -> when (SweepPolicy.valueOf(choice)) {
                SweepPolicy.RELAY -> Triple(
                    context.config.copy(sweepPolicy = SweepPolicy.RELAY),
                    0,
                    context.config.language.container.sweepSelectRelay()
                )
                SweepPolicy.LEAVE -> Triple(
                    context.config.copy(sweepPolicy = SweepPolicy.LEAVE),
                    0,
                    context.config.language.container.sweepSelectLeave()
                )
            }
            ArchivePolicy::class.simpleName -> when (ArchivePolicy.valueOf(choice)) {
                ArchivePolicy.BY_ANONYMOUS -> Triple(
                    context.config.copy(archivePolicy = ArchivePolicy.BY_ANONYMOUS),
                    0,
                    context.config.language.container.archiveSelectByAnonymous()
                )
                ArchivePolicy.WITH_PROFILE -> Triple(
                    context.config.copy(archivePolicy = ArchivePolicy.WITH_PROFILE),
                    0,
                    context.config.language.container.archiveSelectWithProfile()
                )
                ArchivePolicy.PRIVACY -> Triple(
                    context.config.copy(archivePolicy = ArchivePolicy.PRIVACY),
                    0,
                    context.config.language.container.archiveSelectPrivacy()
                )
            }
            else -> throw IllegalStateException()
        } }.fold(
            onSuccess = { it },
            onFailure = { return Option.Empty }
        )

        return Option(ConfigCommand("p", newConfig, kindName.toString(), choiceName))
    }

}
