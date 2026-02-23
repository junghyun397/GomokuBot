package discord.interact.parse.parsers

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import core.database.entities.GameRecordId
import core.interact.commands.Command
import core.interact.commands.ReplayCommand
import discord.interact.UserInteractionContext
import discord.interact.parse.EmbeddableCommand
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import renju.notation.Renju

object ReplayCommandParser : EmbeddableCommand {

    override suspend fun parseComponent(context: UserInteractionContext<GenericComponentInteractionCreateEvent>): Option<Command> = runCatching {
        val (recordIdRaw, movesRaw, validationKey) =
            when (context.event) {
                is StringSelectInteractionEvent -> context.event.interaction.selectedOptions.first().value
                else -> context.event.componentId
            }
                .split("-")
                .drop(1)

        if (validationKey == context.user.id.validationKey)
            Some(ReplayCommand(GameRecordId(recordIdRaw.toLong()), movesRaw.toInt().coerceIn(0 .. Renju.BOARD_SIZE())))
        else
            None
    }
        .fold(
            onSuccess = { it },
            onFailure = { None }
        )

}
