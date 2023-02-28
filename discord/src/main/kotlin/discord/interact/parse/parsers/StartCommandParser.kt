package discord.interact.parse.parsers

import core.assets.User
import core.assets.UserId
import core.database.repositories.UserProfileRepository
import core.interact.commands.Command
import core.interact.commands.StartCommand
import core.interact.commands.buildBoardProcedure
import core.interact.i18n.LanguageContainer
import core.interact.parse.NamedParser
import core.interact.parse.asParseFailure
import core.session.SessionManager
import core.session.SwapType
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.slash
import discord.assets.COMMAND_PREFIX
import discord.assets.DISCORD_PLATFORM_ID
import discord.assets.extractId
import discord.assets.extractProfile
import discord.interact.UserInteractionContext
import discord.interact.parse.BuildableCommand
import discord.interact.parse.DiscordParseFailure
import discord.interact.parse.EmbeddableCommand
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.structs.*

object StartCommandParser : NamedParser, ParsableCommand, EmbeddableCommand, BuildableCommand {

    override val name = "start"

    override fun getLocalizedName(container: LanguageContainer) = container.startCommand()

    override fun getLocalizedUsages(container: LanguageContainer) = listOf(
        BuildableCommand.Usage(
            usage = "``/${container.startCommand()}`` or ``$COMMAND_PREFIX${container.startCommand()}``",
            description = container.commandUsageStartPVE()
        ),
        BuildableCommand.Usage(
            usage = "``/${container.startCommand()} @mention`` or ``$COMMAND_PREFIX${container.startCommand()} @mention``",
            description = container.commandUsageStartPVP()
        ),
    )

    private suspend fun lookupRequestSent(context: UserInteractionContext<*>, owner: User): Option<DiscordParseFailure> =
        SessionManager.retrieveRequestSessionByOwner(context.bot.sessions, context.guild, owner.id).asOption().map { session ->
            this.asParseFailure("already sent request session", context.guild, owner) { producer, publisher, container ->
                producer.produceRequestAlreadySent(publisher, container, session.opponent)
                    .launch()
                    .map { emptyList() }
            }
        }

    private suspend fun lookupRequestOwner(context: UserInteractionContext<*>, owner: User): Option<DiscordParseFailure> =
        SessionManager.retrieveRequestSession(context.bot.sessions, context.guild, owner.id).asOption().map { session ->
            this.asParseFailure("already has request session", context.guild, owner) { producer, publisher, container ->
                producer.produceRequestAlready(publisher, container, session.owner)
                    .launch()
                    .map { emptyList() }
            }
        }

    private suspend fun lookupRequestOpponent(context: UserInteractionContext<*>, owner: User, opponent: User): Option<DiscordParseFailure> =
        SessionManager.retrieveRequestSession(context.bot.sessions, context.guild, opponent.id).asOption().map {
            this.asParseFailure("try to send request session but $opponent already has request session", context.guild, owner) { producer, publisher, container ->
                producer.produceOpponentRequestAlready(publisher, container, opponent)
                    .launch()
                    .map { emptyList() }
            }
        }

    private suspend fun lookupSessionOwner(context: UserInteractionContext<*>, user: User): Option<DiscordParseFailure> =
        SessionManager.retrieveGameSession(context.bot.sessions, context.guild, user.id).asOption().map { session ->
            this.asParseFailure("already has game session", context.guild, user) { producer, publisher, container ->
                producer.produceSessionAlready(publisher, container)
                    .retrieve()
                    .flatMapOption { IO { SessionManager.appendMessage(context.bot.sessions, session.messageBufferKey, it.messageRef) } }
                    .flatMap { when (context.config.swapType) {
                        SwapType.EDIT -> IO.empty
                        else -> buildBoardProcedure(context.bot, context.guild, context.config, producer, publisher, session)
                    } }
                    .map { emptyList() }
            }
        }

    private suspend fun lookupSessionOpponent(context: UserInteractionContext<*>, user: User, opponent: User): Option<DiscordParseFailure> =
        SessionManager.retrieveGameSession(context.bot.sessions, context.guild, opponent.id).asOption().map {
            this.asParseFailure("try to send request session but $opponent already has game session", context.guild, user) { producer, publisher, container ->
                producer.produceOpponentSessionAlready(publisher, container, opponent)
                    .launch()
                    .map { emptyList() }
            }
        }

    private suspend fun parseActually(context: UserInteractionContext<*>, owner: User, opponent: Option<User>) =
        this.lookupSessionOwner(context, owner)
            .orElse { opponent.flatMap { this.lookupSessionOpponent(context, owner, it) } }
            .orElse { this.lookupRequestSent(context, owner) }
            .orElse { this.lookupRequestOwner(context, owner) }
            .orElse { opponent.flatMap { this.lookupRequestOpponent(context, owner, it) } }
            .fold(
                onEmpty = { Either.Left(StartCommand(opponent = opponent.getOrNull())) },
                onDefined = { Either.Right(it) }
            )

    override suspend fun parseSlash(context: UserInteractionContext<SlashCommandInteractionEvent>): Either<Command, DiscordParseFailure> {
        val owner = context.user
        val opponent = context.event.getOption(context.config.language.container.startCommandOptionOpponent())
            .asOption()
            .flatMap {
                val jdaUser = it.asUser

                Option.cond(!jdaUser.isBot) {
                    UserProfileRepository.retrieveOrInsertUser(context.bot.dbConnection, DISCORD_PLATFORM_ID, jdaUser.extractId()) {
                        jdaUser.extractProfile()
                    }
                }
            }

        return this.parseActually(context, owner, opponent)
    }

    override suspend fun parseText(context: UserInteractionContext<MessageReceivedEvent>, payload: List<String>): Either<Command, DiscordParseFailure> {
        val owner = context.user
        val opponent = context.event.message.mentions.members
            .firstOrNull { !it.user.isBot && it.idLong != owner.givenId.idLong }
            ?.user
            .asOption()
            .map {
                UserProfileRepository.retrieveOrInsertUser(context.bot.dbConnection, DISCORD_PLATFORM_ID, it.extractId()) {
                    it.extractProfile()
                }
            }

        return this.parseActually(context, owner, opponent)
    }

    override suspend fun parseComponent(context: UserInteractionContext<GenericComponentInteractionCreateEvent>): Option<Command> {
        val owner = context.user
        val opponent = context.event.componentId
            .drop(2)
            .toLongOrNull()
            ?.let {
                val opponentUser = context.jdaGuild.retrieveMemberById(it).await()

                UserProfileRepository.retrieveOrInsertUser(context.bot.dbConnection, DISCORD_PLATFORM_ID, UserId(it)) {
                    opponentUser.user.extractProfile()
                }
            }
            ?: return Option.Empty

        return if (
            SessionManager.hasRequestSession(context.bot.sessions, context.guild, owner.id, opponent.id)
            || SessionManager.hasGameSession(context.bot.sessions, context.guild, owner.id, opponent.id)
        )
            Option.Empty
        else
            Option(StartCommand(opponent = opponent))
    }

    override fun buildCommandData(action: CommandListUpdateAction, container: LanguageContainer) =
        action.slash(
            container.startCommand(),
            container.startCommandDescription()
        ) {
            option<net.dv8tion.jda.api.entities.User>(
                container.startCommandOptionOpponent(),
                container.startCommandOptionOpponentDescription(),
                false
            )
        }

}
