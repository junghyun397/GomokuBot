package discord.interact.parse.parsers

import core.database.repositories.UserProfileRepository
import core.interact.commands.Command
import core.interact.commands.RankCommand
import core.interact.commands.RankScope
import core.interact.i18n.LanguageContainer
import core.interact.parse.NamedParser
import core.interact.parse.asParseFailure
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.interactions.commands.subcommand
import discord.assets.DISCORD_PLATFORM_ID
import discord.assets.extractId
import discord.interact.InteractionContext
import discord.interact.parse.BuildableCommand
import discord.interact.parse.DiscordParseFailure
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.structs.*

object RankCommandParser : NamedParser, ParsableCommand, BuildableCommand {

    override val name = "rank"

    private suspend fun parseActually(context: InteractionContext<*>, maybeTarget: Option<net.dv8tion.jda.api.entities.User>): Either<Command, DiscordParseFailure> =
        maybeTarget
            .flatMap { UserProfileRepository.retrieveUser(context.bot.dbConnection, DISCORD_PLATFORM_ID, it.extractId()) }
            .fold(
                onDefined = { Either.Left(RankCommand(RankScope.User(it))) },
                onEmpty = { Either.Right(this.asParseFailure("target user not found", context.guild, context.user) { producer, publisher, container ->
                    producer.produceUserNotFound(publisher, container)
                        .launch()
                        .map { emptyList() }
                }) }
            )

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>): Either<Command, DiscordParseFailure> =
        when (context.event.subcommandName) {
            context.config.language.container.rankCommandSubServer() ->
                Either.Left(RankCommand(RankScope.Guild))
            context.config.language.container.rankCommandSubUser() ->
                context.event.getOption(context.config.language.container.rankCommandOptionPlayer())
                    ?.asUser
                    .asOption()
                    .let { this.parseActually(context, it) }
            else -> Either.Left(RankCommand(RankScope.Global))
        }

    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>, payload: List<String>): Either<Command, DiscordParseFailure> =
        when (payload.getOrNull(1)) {
            context.config.language.container.rankCommandSubServer() ->
                Either.Left(RankCommand(RankScope.Guild))
            context.config.language.container.rankCommandSubUser() ->
                context.event.message.mentions.members.firstOrNull()
                    ?.user
                    .asOption()
                    .let { this.parseActually(context, it) }
            else -> Either.Left(RankCommand(RankScope.Global))
        }

    override fun buildCommandData(action: CommandListUpdateAction, container: LanguageContainer) =
        action.slash(
            container.rankCommand(),
            container.rankCommandDescription()
        ) {
            subcommand(container.rankCommandSubGlobal(), container.rankCommandSubGlobalDescription())
            subcommand(container.rankCommandSubServer(), container.rankCommandSubServerDescription())
            subcommand(container.rankCommandSubUser(), container.rankCommandSubUserDescription()) {
                option<net.dv8tion.jda.api.entities.User>(
                    container.rankCommandOptionPlayer(),
                    container.rankCommandOptionPlayerDescription(),
                    required = true
                )
            }
        }

}
