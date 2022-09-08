package discord.interact.parse.parsers

import core.interact.commands.NavigateCommand
import core.session.entities.NavigateState
import core.session.entities.PageNavigateState
import discord.assets.EMOJI_LEFT
import discord.assets.EMOJI_RIGHT
import discord.interact.InteractionContext
import discord.interact.parse.NavigableCommand
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import utils.structs.Option
import utils.structs.asOption
import utils.structs.flatMap

object NavigateCommandParser : NavigableCommand {

    private fun matchIsForward(emoji: UnicodeEmoji) =
        when (emoji) {
            EMOJI_RIGHT -> true
            EMOJI_LEFT -> false
            else -> null
        }

    override suspend fun parseReaction(context: InteractionContext<GenericMessageReactionEvent>, state: NavigateState) =
        state
            .takeIf { it is PageNavigateState }
            .asOption()
            .flatMap {
                when (val isForward = this.matchIsForward(context.event.reaction.emoji.asUnicode())) {
                    null -> Option.Empty
                    else -> Option(NavigateCommand(state as PageNavigateState, isForward))
                }
            }


}
