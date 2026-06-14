package discord.interact.parse.parsers

import core.interact.commands.Direction
import core.interact.commands.FocusCommand
import core.interact.parse.CommandParser
import core.session.SessionManager
import core.session.entities.BoardNavigationState
import core.session.entities.NavigationState
import discord.assets.*
import discord.interact.UserInteractionContext
import discord.interact.parse.NavigableCommand
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent

object FocusCommandParser : CommandParser, NavigableCommand {

    override val name = "focus"

    private fun matchDirection(emoji: UnicodeEmoji) =
        when (emoji) {
            EMOJI_LEFT -> Direction.LEFT
            EMOJI_DOWN -> Direction.DOWN
            EMOJI_UP -> Direction.UP
            EMOJI_RIGHT -> Direction.RIGHT
            EMOJI_FOCUS -> Direction.CENTER
            else -> null
        }

    override suspend fun parseReaction(context: UserInteractionContext<GenericMessageReactionEvent>, state: NavigationState): FocusCommand? {
        val sessionId = SessionManager.findGameSessionId(context.bot.sessions, context.channel.id, context.user.id)
            ?: return null
        val boardState = state as? BoardNavigationState
            ?: return null
        val direction = this.matchDirection(context.event.reaction.emoji.asUnicode())
            ?: return null

        return FocusCommand(boardState, sessionId, direction, context.event.messageRef())
    }

}
