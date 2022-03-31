package discord.interact.command.parsers

import core.assets.Order
import core.assets.User
import dev.minn.jda.ktx.interactions.slash
import discord.interact.command.ParseFailure
import core.interact.commands.Command
import core.interact.commands.ResignCommand
import core.interact.i18n.LanguageContainer
import core.session.SessionManager
import discord.assets.extractUser
import discord.interact.InteractionContext
import discord.interact.command.BuildableCommand
import discord.interact.command.ParsableCommand
import discord.interact.command.asParseFailure
import discord.interact.message.DiscordMessageBinder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.monads.Either
import utils.monads.Option

object ResignCommandParser : ParsableCommand, BuildableCommand {

    override val name = "resign"

    private suspend fun lookupSession(context: InteractionContext<*>, user: User): Option<ParseFailure> =
        if (SessionManager.retrieveGameSession(context.botContext.sessionRepository, context.config.id, user.id) == null)
            Option.Some(this.asParseFailure("$user surrendered but session not found") { _, publisher ->
                DiscordMessageBinder.bindLanguageGuide(publisher).map { it.launch(); Order.UNIT }
            })
        else Option.Empty

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>) =
        this.lookupSession(context, context.event.user.extractUser()).fold(
            onEmpty = { Either.Left(ResignCommand(context.config.language.container.resignCommand())) },
            onDefined = { Either.Right(it) }
        )

    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>) =
        this.lookupSession(context, context.event.author.extractUser()).fold(
            onEmpty = { Either.Left(ResignCommand(context.config.language.container.resignCommand())) },
            onDefined = { Either.Right(it) }
        )

    override fun buildCommandData(action: CommandListUpdateAction, languageContainer: LanguageContainer) =
        action.slash(
            languageContainer.resignCommand(),
            languageContainer.ratingCommandDescription()
        )

}
