package discord.interact.parse.parsers

import core.assets.UNICODE_LEFT
import core.assets.UNICODE_RIGHT
import core.interact.commands.Command
import core.interact.commands.NavigateCommand
import core.session.entities.NavigateState
import discord.interact.InteractionContext
import discord.interact.parse.NavigableCommand
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import utils.structs.Option

object NavigateCommandParser : NavigableCommand {

    private fun matchIsForward(emoji: String) =
        when (emoji) {
            UNICODE_RIGHT -> true
            UNICODE_LEFT -> false
            else -> null
        }

    override suspend fun parseReaction(context: InteractionContext<MessageReactionAddEvent>, state: NavigateState): Option<Command> {
        val isForward = this.matchIsForward(context.event.reactionEmote.emoji)

        return if (isForward == null)
            Option.Empty
        else
            Option(NavigateCommand("*n", state, isForward))
    }

}
