package discord.interact.parse

import core.database.DatabaseConnection
import core.interact.commands.Command
import core.session.SessionPool
import discord.interact.UserInteractionContext
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

interface EmbeddableCommand {

    context(dbConnection: DatabaseConnection, sessions: SessionPool)
    suspend fun parseComponent(context: UserInteractionContext<GenericComponentInteractionCreateEvent>): Command?

}
