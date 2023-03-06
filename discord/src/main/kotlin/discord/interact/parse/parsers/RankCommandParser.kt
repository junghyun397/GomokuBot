package discord.interact.parse.parsers

import core.database.repositories.UserProfileRepository
import core.interact.commands.Command
import core.interact.commands.RankCommand
import core.interact.commands.RankScope
import core.interact.i18n.LanguageContainer
import core.interact.parse.CommandParser
import core.interact.parse.asParseFailure
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.interactions.commands.subcommand
import discord.assets.COMMAND_PREFIX
import discord.assets.DISCORD_PLATFORM_ID
import discord.assets.extractId
import discord.interact.UserInteractionContext
import discord.interact.parse.BuildableCommand
import discord.interact.parse.DiscordParseFailure
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.structs.*

object RankCommandParser : CommandParser, ParsableCommand, BuildableCommand {

    override val name = "rank"

    override fun getLocalizedName(container: LanguageContainer) = container.rankCommand()

    override fun getLocalizedUsages(container: LanguageContainer) = listOf(
        BuildableCommand.Usage(
            usage = "``/${container.rankCommand()} ${container.rankCommandSubGlobal()}`` or ``$COMMAND_PREFIX${container.rankCommand()}``",
            description = container.commandUsageRankGlobal()
        ),
        BuildableCommand.Usage(
            usage = "``/${container.rankCommand()} ${container.rankCommandSubServer()}`` or ``$COMMAND_PREFIX${container.rankCommand()} ${container.rankCommandSubServer()}``",
            description = container.commandUsageRankServer()
        ),
        BuildableCommand.Usage(
            usage = "``/${container.rankCommand()} ${container.rankCommandSubUser()} @mention`` or ``$COMMAND_PREFIX${container.rankCommand()} ${container.rankCommandSubUser()} @mention``",
            description = container.commandUsageRankUser()
        ),
    )

    private suspend fun parseUserRank(context: UserInteractionContext<*>, maybeTarget: Option<net.dv8tion.jda.api.entities.User>): Either<Command, DiscordParseFailure> =
        maybeTarget
            .flatMap { UserProfileRepository.retrieveUser(context.bot.dbConnection, DISCORD_PLATFORM_ID, it.extractId()) }
            .fold(
                onDefined = { Either.Left(RankCommand(RankScope.User(it))) },
                onEmpty = { Either.Right(this.asParseFailure("target user not found", context.guild, context.user) { messagingService, publisher, container ->
                    messagingService.buildUserNotFound(publisher, container)
                        .launch()
                        .map { emptyList() }
                }) }
            )

    override suspend fun parseSlash(context: UserInteractionContext<SlashCommandInteractionEvent>): Either<Command, DiscordParseFailure> =
        when (context.event.subcommandName) {
            context.config.language.container.rankCommandSubServer() ->
                Either.Left(RankCommand(RankScope.Guild(context.guild)))
            context.config.language.container.rankCommandSubUser() ->
                context.event.getOption(context.config.language.container.rankCommandOptionPlayer())
                    ?.asUser
                    .asOption()
                    .let { this.parseUserRank(context, it) }
            else -> Either.Left(RankCommand(RankScope.Global))
        }

    override suspend fun parseText(context: UserInteractionContext<MessageReceivedEvent>, payload: List<String>): Either<Command, DiscordParseFailure> =
        when (payload.getOrNull(1)) {
            context.config.language.container.rankCommandSubServer() ->
                Either.Left(RankCommand(RankScope.Guild(context.guild)))
            context.config.language.container.rankCommandSubUser() ->
                context.event.message.mentions.members.firstOrNull()
                    ?.user
                    .asOption()
                    .let { this.parseUserRank(context, it) }
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
