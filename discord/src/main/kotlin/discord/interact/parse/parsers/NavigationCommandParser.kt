package discord.interact.parse.parsers

import core.interact.commands.NavigationCommand
import core.session.entities.NavigationState
import core.session.entities.PageNavigationState
import discord.assets.EMOJI_LEFT
import discord.assets.EMOJI_RIGHT
import discord.interact.InteractionContext
import discord.interact.parse.NavigableCommand
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import utils.structs.asOption
import utils.structs.flatMap
import utils.structs.map

object NavigationCommandParser : NavigableCommand {

    private fun matchIsForward(emoji: UnicodeEmoji) =
        when (emoji) {
            EMOJI_RIGHT -> true
            EMOJI_LEFT -> false
            else -> null
        }

    override suspend fun parseReaction(context: InteractionContext<GenericMessageReactionEvent>, state: NavigationState) =
        state
            .takeIf { it is PageNavigationState }
            .asOption()
            .flatMap {
                this.matchIsForward(context.event.reaction.emoji.asUnicode())
                    .asOption()
                    .map { NavigationCommand(state as PageNavigationState, it) }
            }


}
