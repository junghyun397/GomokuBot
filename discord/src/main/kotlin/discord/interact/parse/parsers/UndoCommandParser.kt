package discord.interact.parse.parsers

import core.database.DatabaseConnection
import core.interact.commands.ResponseFlag
import core.interact.commands.UndoCommand
import core.interact.i18n.LanguageContainer
import core.interact.parse.SessionSideParser
import core.session.SessionPool
import core.session.entities.PvpGameSession
import dev.minn.jda.ktx.interactions.commands.slash
import discord.assets.COMMAND_PREFIX
import discord.interact.UserInteractionContext
import discord.interact.parse.BuildableCommand
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction

object UndoCommandParser : SessionSideParser(), ParsableCommand, BuildableCommand {

    override val name = "undo"

    override fun getLocalizedName(container: LanguageContainer) = container.undoCommand

    override fun getLocalizedUsages(container: LanguageContainer) = listOf(
        BuildableCommand.Usage(
            usage = "`/${container.undoCommand}` or `$COMMAND_PREFIX${container.undoCommand}`",
            description = container.undoCommandDescription
        ),
    )

    context(dbConnection: DatabaseConnection, sessions: SessionPool)
    override suspend fun parseSlash(context: UserInteractionContext<SlashCommandInteractionEvent>) =
        this.retrieveSession(context.channel, context.user).map { (sessionId, session) ->
            UndoCommand(sessionId, if (session is PvpGameSession) ResponseFlag.Defer else ResponseFlag.DeferWindowed)
        }

    context(dbConnection: DatabaseConnection, sessions: SessionPool)
    override suspend fun parseText(context: UserInteractionContext<MessageReceivedEvent>, payload: List<String>) =
        this.retrieveSession(context.channel, context.user).map { (sessionId, session) ->
            UndoCommand(sessionId, if (session is PvpGameSession) ResponseFlag.Defer else ResponseFlag.DeferWindowed)
        }

    override fun buildCommandData(action: CommandListUpdateAction, container: LanguageContainer) =
        action.slash(container.undoCommand, container.undoCommandDescription)

}
