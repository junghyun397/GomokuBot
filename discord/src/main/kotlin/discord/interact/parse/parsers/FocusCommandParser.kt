package discord.interact.parse.parsers

import core.interact.commands.Direction
import core.interact.commands.FocusCommand
import core.interact.parse.NamedParser
import core.session.SessionManager
import core.session.entities.BoardNavigateState
import core.session.entities.NavigateState
import discord.assets.*
import discord.interact.InteractionContext
import discord.interact.parse.NavigableCommand
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import utils.lang.and
import utils.structs.Option
import utils.structs.asOption
import utils.structs.flatMap
import utils.structs.map

object FocusCommandParser : NamedParser, NavigableCommand {

    override val name = "focus"

    private fun matchDirection(emoji: UnicodeEmoji) =
        when (emoji) {
            EMOJI_LEFT -> Direction.LEFT
            EMOJI_DOWN -> Direction.DOWN
            EMOJI_UP -> Direction.UP
            EMOJI_RIGHT -> Direction.RIGHT
            EMOJI_FOCUS -> Direction.FOCUS
            else -> null
        }

    override suspend fun parseReaction(context: InteractionContext<GenericMessageReactionEvent>, state: NavigateState) =
        SessionManager.retrieveGameSession(context.bot.sessions, context.guild, context.user.id)
            ?.takeIf { state is BoardNavigateState }
            .asOption()
            .flatMap { session ->
                when (val direction = this.matchDirection(context.event.reaction.emoji.asUnicode())) {
                    null -> Option.Empty
                    else -> Option(session and direction)
                }
            }
            .map { (session, direction) -> FocusCommand(state as BoardNavigateState, session, direction) }

}
