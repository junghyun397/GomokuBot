package discord.interact.parse.parsers

import core.assets.User
import core.assets.UserId
import core.database.DatabaseManager
import core.interact.Order
import core.interact.commands.Command
import core.interact.commands.StartCommand
import core.interact.commands.buildBoardSequence
import core.interact.i18n.LanguageContainer
import core.interact.parse.NamedParser
import core.interact.parse.asParseFailure
import core.session.SessionManager
import dev.minn.jda.ktx.await
import dev.minn.jda.ktx.interactions.option
import dev.minn.jda.ktx.interactions.slash
import discord.assets.DISCORD_PLATFORM_ID
import discord.assets.buildNewProfile
import discord.assets.extractId
import discord.interact.InteractionContext
import discord.interact.parse.BuildableCommand
import discord.interact.parse.DiscordParseFailure
import discord.interact.parse.EmbeddableCommand
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.structs.Either
import utils.structs.Option
import utils.structs.Option.Empty.orElse
import utils.structs.asOption

object StartCommandParser : NamedParser, ParsableCommand, EmbeddableCommand, BuildableCommand {

    override val name = "start"

    private suspend fun lookupRequestSent(context: InteractionContext<*>, owner: User): Option<DiscordParseFailure> =
        SessionManager.retrieveRequestSessionByOwner(context.bot.sessions, context.guild, owner.id).asOption().flatMap { session ->
            Option(this.asParseFailure("already sent request session", owner) { producer, publisher, container ->
                producer.produceRequestAlreadySent(publisher, container, owner, session.opponent).map { it.launch(); Order.Unit }
            })
        }

    private suspend fun lookupRequestOwner(context: InteractionContext<*>, owner: User): Option<DiscordParseFailure> =
        SessionManager.retrieveRequestSession(context.bot.sessions, context.guild, owner.id).asOption().flatMap { session ->
            Option(this.asParseFailure("already has request session", owner) { producer, publisher, container ->
                producer.produceRequestAlready(publisher, container, session.owner, session.opponent).map { it.launch(); Order.Unit }
            })
        }

    private suspend fun lookupRequestOpponent(context: InteractionContext<*>, owner: User, opponent: User): Option<DiscordParseFailure> =
        SessionManager.retrieveRequestSession(context.bot.sessions, context.guild, opponent.id).asOption().flatMap { _ ->
            Option(this.asParseFailure("try to send request session but $opponent already has request session", owner) { producer, publisher, container ->
                producer.produceOpponentRequestAlready(publisher, container, owner, opponent).map { it.launch(); Order.Unit }
            })
        }

    private suspend fun lookupSessionOwner(context: InteractionContext<*>, user: User): Option<DiscordParseFailure> =
        SessionManager.retrieveGameSession(context.bot.sessions, context.guild, user.id).asOption().flatMap { session ->
            Option(this.asParseFailure("already has game session", user) { producer, publisher, container ->
                producer.produceSessionAlready(publisher, container, session.owner)
                    .map { SessionManager.appendMessage(context.bot.sessions, session.messageBufferKey, it.retrieve().messageRef) }
                    .flatMap { buildBoardSequence(context.bot, context.guild, context.config, producer, publisher, session) }
                    .map { Order.Unit }
            })
        }

    private suspend fun lookupSessionOpponent(context: InteractionContext<*>, user: User, opponent: User): Option<DiscordParseFailure> =
        SessionManager.retrieveGameSession(context.bot.sessions, context.guild, opponent.id).asOption().flatMap { _ ->
            Option(this.asParseFailure("try to send request session but $opponent already has game session", user) { producer, publisher, container ->
                producer.produceOpponentSessionAlready(publisher, container, user, opponent).map { it.launch(); Order.Unit }
            })
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
                    DatabaseManager.retrieveUser(context.bot.databaseConnection, DISCORD_PLATFORM_ID, jdaUser.extractId())
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
                DatabaseManager.retrieveOrInsertUser(context.bot.databaseConnection, DISCORD_PLATFORM_ID, it.extractId()) {
                    it.buildNewProfile()
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

                DatabaseManager.retrieveOrInsertUser(context.bot.databaseConnection, DISCORD_PLATFORM_ID, UserId(it)) {
                    opponentUser.user.buildNewProfile()
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
