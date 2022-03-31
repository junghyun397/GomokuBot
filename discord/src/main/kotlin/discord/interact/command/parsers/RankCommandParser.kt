package discord.interact.command.parsers

import dev.minn.jda.ktx.interactions.slash
import discord.interact.command.ParseFailure
import core.interact.commands.Command
import core.interact.commands.RankCommand
import core.interact.i18n.LanguageContainer
import discord.interact.InteractionContext
import discord.interact.command.BuildableCommand
import discord.interact.command.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.monads.Either

object RankCommandParser : ParsableCommand, BuildableCommand {

    override val name = "rank"

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>) =
        Either.Left(RankCommand(context.config.language.container.rankCommand()))

    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>) =
        Either.Left(RankCommand(context.config.language.container.rankCommand()))

    override fun buildCommandData(action: CommandListUpdateAction, languageContainer: LanguageContainer) =
        action.slash(
            languageContainer.rankCommand(),
            languageContainer.rankCommandDescription()
        )

}
