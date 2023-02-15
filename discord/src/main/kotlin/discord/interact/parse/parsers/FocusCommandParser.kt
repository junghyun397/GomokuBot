package discord.interact.parse.parsers

import core.interact.commands.Direction
import core.interact.commands.FocusCommand
import core.interact.parse.NamedParser
import core.session.SessionManager
import core.session.entities.BoardNavigationState
import core.session.entities.NavigationState
import discord.assets.*
import discord.interact.InteractionContext
import discord.interact.parse.NavigableCommand
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import utils.lang.tuple
import utils.structs.asOption
import utils.structs.filter
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
            EMOJI_FOCUS -> Direction.CENTER
            else -> null
        }

    override suspend fun parseReaction(context: InteractionContext<GenericMessageReactionEvent>, state: NavigationState) =
        SessionManager.retrieveGameSession(context.bot.sessions, context.guild, context.user.id)
            .asOption()
            .filter { state is BoardNavigationState }
            .flatMap { session ->
                this.matchDirection(context.event.reaction.emoji.asUnicode())
                    .asOption()
                    .map { tuple(session, it) }
            }
            .map { (session, direction) -> FocusCommand(state as BoardNavigationState, session, direction) }

}
