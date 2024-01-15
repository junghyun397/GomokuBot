package discord.interact.parse.parsers

import core.interact.commands.RecentRecordsCommand
import core.interact.i18n.LanguageContainer
import core.interact.parse.CommandParser
import dev.minn.jda.ktx.interactions.commands.slash
import discord.assets.COMMAND_PREFIX
import discord.interact.UserInteractionContext
import discord.interact.parse.BuildableCommand
import discord.interact.parse.EmbeddableCommand
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.structs.Either
import utils.structs.Option

object RecentRecordsCommandParser : CommandParser, ParsableCommand, BuildableCommand, EmbeddableCommand {

    override val name = "records"

    override fun getLocalizedName(container: LanguageContainer) = container.replayCommand()

    override fun getLocalizedUsages(container: LanguageContainer) = listOf(
        BuildableCommand.Usage(
            usage = "``/${container.replayCommand()}`` or ``$COMMAND_PREFIX${container.replayCommand()}``",
            description = container.commandUsageReplay()
        ),
    )

    override suspend fun parseSlash(context: UserInteractionContext<SlashCommandInteractionEvent>) =
        Either.Left(RecentRecordsCommand(false))

    override suspend fun parseText(context: UserInteractionContext<MessageReceivedEvent>, payload: List<String>) =
        Either.Left(RecentRecordsCommand(false))

    override fun buildCommandData(action: CommandListUpdateAction, container: LanguageContainer) =
        action.apply {
            slash(
                container.replayCommand(),
                container.replayCommandDescription()
            )
        }

    override suspend fun parseComponent(context: UserInteractionContext<GenericComponentInteractionCreateEvent>) =
        Option(RecentRecordsCommand(true))

}
