package discord.interact.parse.parsers

import core.database.DatabaseConnection
import core.interact.commands.BoardCommand
import core.interact.i18n.LanguageContainer
import core.interact.parse.SessionSideParser
import core.session.SessionPool
import dev.minn.jda.ktx.interactions.commands.slash
import discord.assets.COMMAND_PREFIX
import discord.interact.UserInteractionContext
import discord.interact.parse.BuildableCommand
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction

object BoardCommandParser: SessionSideParser(), ParsableCommand, BuildableCommand {

    override val name = "board"

    override fun getLocalizedName(container: LanguageContainer) = container.boardCommand

    override fun getLocalizedUsages(container: LanguageContainer) = listOf(
        BuildableCommand.Usage(
            usage = "`/${container.boardCommand}` or `$COMMAND_PREFIX${container.boardCommand}`",
            description = container.commandUsageBoard
        ),
    )

    context(dbConnection: DatabaseConnection, sessions: SessionPool)
    override suspend fun parseSlash(context: UserInteractionContext<SlashCommandInteractionEvent>) =
        this.retrieveSessionId(context.channel, context.user).map { sessionId ->
            BoardCommand(sessionId)
        }

    context(dbConnection: DatabaseConnection, sessions: SessionPool)
    override suspend fun parseText(context: UserInteractionContext<MessageReceivedEvent>, payload: List<String>) =
        this.retrieveSessionId(context.channel, context.user).map { sessionId ->
            BoardCommand(sessionId)
        }

    override fun buildCommandData(action: CommandListUpdateAction, container: LanguageContainer) =
        action.slash(
            container.boardCommand,
            container.boardCommandDescription
        )

}
