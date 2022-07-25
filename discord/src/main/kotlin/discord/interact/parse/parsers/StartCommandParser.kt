package discord.interact.parse.parsers

import core.assets.User
import core.assets.UserId
import core.database.repositories.UserProfileRepository
import core.interact.commands.Command
import core.interact.commands.StartCommand
import core.interact.commands.buildBoardSequence
import core.interact.i18n.LanguageContainer
import core.interact.parse.NamedParser
import core.interact.parse.asParseFailure
import core.session.SessionManager
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.slash
import discord.assets.DISCORD_PLATFORM_ID
import discord.assets.extractId
import discord.assets.extractProfile
import discord.interact.InteractionContext
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

    private suspend fun lookupRequestSent(context: InteractionContext<*>, owner: User): Option<DiscordParseFailure> =
        SessionManager.retrieveRequestSessionByOwner(context.bot.sessions, context.guild, owner.id).asOption().map { session ->
            this.asParseFailure("already sent request session", owner) { producer, publisher, container ->
                producer.produceRequestAlreadySent(publisher, container, owner, session.opponent)
                    .flatMap { it.launch() }
                    .map { emptyList() }
            }
        }

    private suspend fun lookupRequestOwner(context: InteractionContext<*>, owner: User): Option<DiscordParseFailure> =
        SessionManager.retrieveRequestSession(context.bot.sessions, context.guild, owner.id).asOption().map { session ->
            this.asParseFailure("already has request session", owner) { producer, publisher, container ->
                producer.produceRequestAlready(publisher, container, session.owner, session.opponent)
                    .flatMap { it.launch() }
                    .map { emptyList() }
            }
        }

    private suspend fun lookupRequestOpponent(context: InteractionContext<*>, owner: User, opponent: User): Option<DiscordParseFailure> =
        SessionManager.retrieveRequestSession(context.bot.sessions, context.guild, opponent.id).asOption().map { _ ->
            this.asParseFailure("try to send request session but $opponent already has request session", owner) { producer, publisher, container ->
                producer.produceOpponentRequestAlready(publisher, container, owner, opponent)
                    .flatMap { it.launch() }
                    .map { emptyList() }
            }
        }

    private suspend fun lookupSessionOwner(context: InteractionContext<*>, user: User): Option<DiscordParseFailure> =
        SessionManager.retrieveGameSession(context.bot.sessions, context.guild, user.id).asOption().map { session ->
            this.asParseFailure("already has game session", user) { producer, publisher, container ->
                producer.produceSessionAlready(publisher, container, session.owner)
                    .flatMap { IO { SessionManager.appendMessage(context.bot.sessions, session.messageBufferKey, it.retrieve().messageRef) } }
                    .flatMap { buildBoardSequence(context.bot, context.guild, context.config, producer, publisher, session) }
                    .map { emptyList() }
            }
        }

    private suspend fun lookupSessionOpponent(context: InteractionContext<*>, user: User, opponent: User): Option<DiscordParseFailure> =
        SessionManager.retrieveGameSession(context.bot.sessions, context.guild, opponent.id).asOption().map { _ ->
            this.asParseFailure("try to send request session but $opponent already has game session", user) { producer, publisher, container ->
                producer.produceOpponentSessionAlready(publisher, container, user, opponent)
                    .flatMap { it.launch() }
                    .map { emptyList() }
            }
        }

    private suspend fun parseActually(context: InteractionContext<*>, owner: User, opponent: Option<User>) =
        this.lookupSessionOwner(context, owner)
            .orElse { opponent.flatMap { this.lookupSessionOpponent(context, owner, it) } }
            .orElse { this.lookupRequestSent(context, owner) }
            .orElse { this.lookupRequestOwner(context, owner) }
            .orElse { opponent.flatMap { this.lookupRequestOpponent(context, owner, it) } }
            .fold(
                onEmpty = { Either.Left(StartCommand(opponent = opponent.getOrNull())) },
                onDefined = { Either.Right(it) }
            )

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>): Either<Command, DiscordParseFailure> {
        val owner = context.user
        val opponent = context.event.getOption(context.config.language.container.startCommandOptionOpponent())
            .asOption()
            .flatMap {
                val jdaUser = it.asUser

                if (jdaUser.isBot)
                    Option.Empty
                else
                    Option(UserProfileRepository.retrieveOrInsertUser(context.bot.dbConnection, DISCORD_PLATFORM_ID, jdaUser.extractId()) {
                        jdaUser.extractProfile()
                    })
            }

        return this.parseActually(context, owner, opponent)
    }

    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>, payload: List<String>): Either<Command, DiscordParseFailure> {
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

    override suspend fun parseButton(context: InteractionContext<GenericComponentInteractionCreateEvent>): Option<Command> {
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
