package discord.interact.parse.parsers

import core.interact.commands.ResignCommand
import core.interact.i18n.LanguageContainer
import core.interact.parse.SessionSideParser
import dev.minn.jda.ktx.interactions.slash
import discord.assets.extractUser
import discord.interact.InteractionContext
import discord.interact.message.DiscordButtons
import discord.interact.parse.BuildableCommand
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction

object ResignCommandParser : SessionSideParser<Message, DiscordButtons>(), ParsableCommand, BuildableCommand {

    override val name = "resign"

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>) =
        this.retrieveSessionWith(context.botContext, context.guild, context.event.user.extractUser()).mapLeft {
            ResignCommand(context.config.language.container.resignCommand(), it)
        }

    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>) =
        this.retrieveSessionWith(context.botContext, context.guild, context.event.author.extractUser()).mapLeft {
            ResignCommand(context.config.language.container.resignCommand(), it)
        }

    override fun buildCommandData(action: CommandListUpdateAction, languageContainer: LanguageContainer) =
        action.slash(
            languageContainer.resignCommand(),
            languageContainer.ratingCommandDescription()
        )

}
