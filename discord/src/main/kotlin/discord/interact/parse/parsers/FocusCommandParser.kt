package discord.interact.parse.parsers

import core.assets.*
import core.interact.commands.Direction
import core.interact.commands.FocusCommand
import core.session.SessionManager
import core.session.entities.NavigateState
import discord.interact.InteractionContext
import discord.interact.parse.NavigableCommand
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import utils.structs.Option
import utils.structs.asOption
import utils.structs.flatMap
import utils.structs.map

object FocusCommandParser : NavigableCommand {

    private fun matchDirection(emoji: String) =
        when (emoji) {
            UNICODE_LEFT -> Direction.LEFT
            UNICODE_DOWN -> Direction.DOWN
            UNICODE_UP -> Direction.UP
            UNICODE_RIGHT -> Direction.RIGHT
            UNICODE_FOCUS -> Direction.FOCUS
            else -> null
        }

    override suspend fun parseReaction(context: InteractionContext<MessageReactionAddEvent>, state: NavigateState) =
        SessionManager.retrieveGameSession(context.bot.sessions, context.guild, context.user.id)
            .asOption()
            .flatMap {
                val direction = this.matchDirection(context.event.reactionEmote.emoji)

                if (direction == null)
                    Option.Empty
                else
                    Option(it to direction)
            }
            .map { FocusCommand("*f", state, it.first, it.second) }

}
