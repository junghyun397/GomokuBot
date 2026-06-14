package discord.interact.parse.parsers

import arrow.core.Either
import core.interact.commands.ReplayListCommand
import core.interact.i18n.LanguageContainer
import core.interact.parse.CommandParser
import dev.minn.jda.ktx.interactions.commands.slash
import discord.assets.COMMAND_PREFIX
import discord.assets.messageRef
import discord.interact.UserInteractionContext
import discord.interact.parse.BuildableCommand
import discord.interact.parse.EmbeddableCommand
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction

object ReplayListCommandParser : CommandParser, ParsableCommand, BuildableCommand, EmbeddableCommand {

    override val name = "replay-list"

    override fun getLocalizedName(container: LanguageContainer) = container.replayCommand()

    override fun getLocalizedUsages(container: LanguageContainer) = listOf(
        BuildableCommand.Usage(
            usage = "``/${container.replayCommand()}`` or ``$COMMAND_PREFIX${container.replayCommand()}``",
            description = container.commandUsageReplay()
        ),
    )

    override suspend fun parseSlash(context: UserInteractionContext<SlashCommandInteractionEvent>) =
        Either.Right(ReplayListCommand(null))

    override suspend fun parseText(context: UserInteractionContext<MessageReceivedEvent>, payload: List<String>) =
        Either.Right(ReplayListCommand(null))

    override fun buildCommandData(action: CommandListUpdateAction, container: LanguageContainer) =
        action.apply {
            slash(
                container.replayCommand(),
                container.replayCommandDescription()
            )
        }

    override suspend fun parseComponent(context: UserInteractionContext<GenericComponentInteractionCreateEvent>) = runCatching {
        if (context.event.componentId.split("-")[1] == context.user.id.validationKey)
            ReplayListCommand(context.event.message.messageRef())
        else
            null
    }
        .fold(
            onSuccess = { it },
            onFailure = { null }
        )

}
