package discord.interact.parse.parsers

import core.interact.commands.Command
import core.interact.commands.NavigateCommand
import core.session.entities.NavigateState
import discord.assets.EMOJI_LEFT
import discord.assets.EMOJI_RIGHT
import discord.interact.InteractionContext
import discord.interact.parse.NavigableCommand
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import utils.structs.Option

object NavigateCommandParser : NavigableCommand {

    private fun matchIsForward(emoji: UnicodeEmoji) =
        when (emoji) {
            EMOJI_RIGHT -> true
            EMOJI_LEFT -> false
            else -> null
        }

    override suspend fun parseReaction(context: InteractionContext<MessageReactionAddEvent>, state: NavigateState): Option<Command> {
        val isForward = this.matchIsForward(context.event.reaction.emoji.asUnicode())

        return if (isForward == null)
            Option.Empty
        else
            Option(NavigateCommand("navigate", state, isForward))
    }

}
