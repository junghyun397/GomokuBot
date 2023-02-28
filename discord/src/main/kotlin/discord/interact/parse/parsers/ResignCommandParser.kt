package discord.interact.parse.parsers

import core.interact.commands.ResignCommand
import core.interact.i18n.LanguageContainer
import core.interact.parse.SessionSideParser
import dev.minn.jda.ktx.interactions.commands.slash
import discord.assets.COMMAND_PREFIX
import discord.interact.UserInteractionContext
import discord.interact.message.DiscordComponents
import discord.interact.message.DiscordMessageData
import discord.interact.parse.BuildableCommand
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.structs.mapLeft

object ResignCommandParser : SessionSideParser<DiscordMessageData, DiscordComponents>(), ParsableCommand, BuildableCommand {

    override val name = "resign"

    override fun getLocalizedName(container: LanguageContainer) = container.resignCommand()

    override fun getLocalizedUsages(container: LanguageContainer) = listOf(
        BuildableCommand.Usage(
            usage = "``/${container.resignCommand()}`` or ``$COMMAND_PREFIX${container.resignCommand()}``",
            description = container.commandUsageResign()
        ),
    )

    override suspend fun parseSlash(context: UserInteractionContext<SlashCommandInteractionEvent>) =
        this.retrieveSession(context.bot, context.guild, context.user).mapLeft { session ->
            ResignCommand(session)
        }

    override suspend fun parseText(context: UserInteractionContext<MessageReceivedEvent>, payload: List<String>) =
        this.retrieveSession(context.bot, context.guild, context.user).mapLeft { session ->
            ResignCommand(session)
        }

    override fun buildCommandData(action: CommandListUpdateAction, container: LanguageContainer) =
        action.slash(
            container.resignCommand(),
            container.resignCommandDescription()
        )

}
