package discord.interact.parse.parsers

import core.assets.*
import core.interact.commands.Direction
import core.interact.commands.FocusCommand
import core.session.SessionManager
import discord.assets.extractUser
import discord.interact.InteractionContext
import discord.interact.parse.NavigableCommand
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import utils.structs.asOption

object FocusCommandParser : NavigableCommand {

    private fun matchDirection(emoji: String) =
        when (emoji) {
            UNICODE_LEFT -> Direction.LEFT
            UNICODE_DOWN -> Direction.DOWN
            UNICODE_UP -> Direction.UP
            UNICODE_RIGHT -> Direction.RIGHT
            UNICODE_FOCUS -> Direction.FOCUS
            else -> throw Exception()
        }

    override suspend fun parseReaction(context: InteractionContext<MessageReactionAddEvent>) =
        SessionManager.retrieveGameSession(context.bot.sessionRepository, context.guild.id, context.event.user!!.extractUser().id)
            .asOption()
            .map { FocusCommand("*f", it, this.matchDirection(context.event.reactionEmote.emoji)) }

}