package discord.interact.parse

import core.interact.commands.Command
import core.session.entities.NavigationState
import discord.interact.UserInteractionContext
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import utils.structs.Option

interface NavigableCommand {

    suspend fun parseReaction(context: UserInteractionContext<GenericMessageReactionEvent>, state: NavigationState): Option<Command>

}
