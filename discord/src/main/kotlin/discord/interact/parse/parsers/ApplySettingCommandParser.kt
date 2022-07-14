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
import utils.structs.Option
import utils.structs.asOption
import utils.structs.map

object ApplySettingCommandParser : EmbeddableCommand {

    override suspend fun parseButton(context: InteractionContext<GenericComponentInteractionCreateEvent>): Option<Command> {
        val (kind, choice) = run {
            if (context.event !is SelectMenuInteractionEvent)
                context.event.componentId.split("-").drop(1)
            else
                context.event.interaction.selectedOptions.first().value.split("-")
        }

        return runCatching { when (kind) {
            BoardStyle::class.simpleName -> when (BoardStyle.valueOf(choice)) {
                BoardStyle.IMAGE -> Triple(
                    context.config.copy(boardStyle = BoardStyle.IMAGE),
                    "",
                    context.config.language.container.styleSelectImage()
                )
                BoardStyle.TEXT -> Triple(
                    context.config.copy(boardStyle = BoardStyle.TEXT),
                    "",
                    context.config.language.container.styleSelectText()
                )
                BoardStyle.SOLID_TEXT -> Triple(
                    context.config.copy(boardStyle = BoardStyle.SOLID_TEXT),
                    "",
                    context.config.language.container.styleSelectSolidText()
                )
                BoardStyle.UNICODE -> Triple(
                    context.config.copy(boardStyle = BoardStyle.UNICODE),
                    "",
                    context.config.language.container.styleSelectUnicodeText()
                )
            }
            FocusPolicy::class.simpleName -> when (FocusPolicy.valueOf(choice)) {
                FocusPolicy.INTELLIGENCE -> Triple(
                    context.config.copy(focusPolicy = FocusPolicy.INTELLIGENCE),
                    "",
                    context.config.language.container.focusSelectIntelligence()
                )
                FocusPolicy.FALLOWING -> Triple(
                    context.config.copy(focusPolicy = FocusPolicy.FALLOWING),
                    "",
                    context.config.language.container.focusSelectFallowing()
                )
            }
            SweepPolicy::class.simpleName -> when (SweepPolicy.valueOf(choice)) {
                SweepPolicy.RELAY -> Triple(
                    context.config.copy(sweepPolicy = SweepPolicy.RELAY),
                    "",
                    context.config.language.container.sweepSelectRelay()
                )
                SweepPolicy.LEAVE -> Triple(
                    context.config.copy(sweepPolicy = SweepPolicy.LEAVE),
                    "",
                    context.config.language.container.sweepSelectLeave()
                )
            }
            ArchivePolicy::class.simpleName -> when (ArchivePolicy.valueOf(choice)) {
                ArchivePolicy.BY_ANONYMOUS -> Triple(
                    context.config.copy(archivePolicy = ArchivePolicy.BY_ANONYMOUS),
                    "",
                    context.config.language.container.archiveSelectByAnonymous()
                )
                ArchivePolicy.WITH_PROFILE -> Triple(
                    context.config.copy(archivePolicy = ArchivePolicy.WITH_PROFILE),
                    "",
                    context.config.language.container.archiveSelectWithProfile()
                )
                ArchivePolicy.PRIVACY -> Triple(
                    context.config.copy(archivePolicy = ArchivePolicy.PRIVACY),
                    "",
                    context.config.language.container.archiveSelectPrivacy()
                )
            }
            else -> throw IllegalStateException()
        } }
            .asOption()
            .map { (newConfig, kindName, choiceName) ->
                ApplySettingCommand("p", newConfig, kindName, choiceName)
            }
    }

}
