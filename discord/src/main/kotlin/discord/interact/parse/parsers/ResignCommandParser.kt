package discord.interact.parse.parsers

import core.interact.commands.ResignCommand
import core.interact.i18n.LanguageContainer
import core.interact.parse.SessionSideParser
import dev.minn.jda.ktx.interactions.commands.slash
import discord.assets.DiscordMessageData
import discord.interact.InteractionContext
import discord.interact.message.DiscordComponents
import discord.interact.parse.BuildableCommand
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.structs.mapLeft

object ResignCommandParser : SessionSideParser<DiscordMessageData, DiscordComponents>(), ParsableCommand, BuildableCommand {

    override val name = "resign"

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>) =
        this.retrieveSession(context.bot, context.guild, context.user).mapLeft { session ->
            ResignCommand(session)
        }

    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>, payload: List<String>) =
        this.retrieveSession(context.bot, context.guild, context.user).mapLeft { session ->
            ResignCommand(session)
        }

    override fun buildCommandData(action: CommandListUpdateAction, container: LanguageContainer) =
        action.slash(
            container.resignCommand(),
            container.resignCommandDescription()
        )

}
