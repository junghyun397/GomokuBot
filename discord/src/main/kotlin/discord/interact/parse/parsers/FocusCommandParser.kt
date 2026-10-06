package discord.interact.parse.parsers

import core.interact.commands.FocusCommand
import core.interact.commands.FocusDirection
import core.interact.parse.CommandParser
import core.session.SessionManager
import core.session.SessionPool
import discord.assets.*
import discord.interact.UserInteractionContext
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent

object FocusCommandParser : CommandParser {

    override val name = "focus"

    private fun matchDirection(emoji: UnicodeEmoji) =
        when (emoji) {
            EMOJI_LEFT -> FocusDirection.LEFT
            EMOJI_DOWN -> FocusDirection.DOWN
            EMOJI_UP -> FocusDirection.UP
            EMOJI_RIGHT -> FocusDirection.RIGHT
            EMOJI_FOCUS -> FocusDirection.CENTER
            else -> null
        }

    context(sessions: SessionPool)
    suspend fun parseReaction(context: UserInteractionContext<GenericMessageReactionEvent>): FocusCommand? {
        val sessionId = SessionManager.findGameSessionId(context.channel.id, context.user.id)
            ?: return null
        val direction = this.matchDirection(context.event.reaction.emoji.asUnicode())
            ?: return null

        return FocusCommand(sessionId, direction)
    }

}
