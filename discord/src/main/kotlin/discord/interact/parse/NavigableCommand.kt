package discord.interact.parse

import core.interact.commands.Command
import core.session.entities.NavigateState
import discord.interact.InteractionContext
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import utils.structs.Option

interface NavigableCommand {

    suspend fun parseReaction(context: InteractionContext<MessageReactionAddEvent>, state: NavigateState): Option<Command>

}
