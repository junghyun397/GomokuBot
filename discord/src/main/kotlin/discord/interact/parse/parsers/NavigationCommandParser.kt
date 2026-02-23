package discord.interact.parse.parsers

import arrow.core.toOption
import core.interact.commands.NavigationCommand
import core.session.entities.NavigationState
import core.session.entities.PageNavigationState
import discord.assets.EMOJI_LEFT
import discord.assets.EMOJI_RIGHT
import discord.interact.UserInteractionContext
import discord.interact.parse.NavigableCommand
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent

object NavigationCommandParser : NavigableCommand {

    private fun matchIsForward(emoji: UnicodeEmoji) =
        when (emoji) {
            EMOJI_RIGHT -> true
            EMOJI_LEFT -> false
            else -> null
        }

    override suspend fun parseReaction(context: UserInteractionContext<GenericMessageReactionEvent>, state: NavigationState) =
        state
            .takeIf { it is PageNavigationState }
            .toOption()
            .flatMap {
                this.matchIsForward(context.event.reaction.emoji.asUnicode())
                    .toOption()
                    .map { NavigationCommand(state as PageNavigationState, it) }
            }


}
