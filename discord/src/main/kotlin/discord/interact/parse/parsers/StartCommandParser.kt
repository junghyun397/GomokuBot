package discord.interact.parse.parsers

import core.assets.User
import core.assets.UserId
import core.interact.Order
import core.interact.commands.Command
import core.interact.commands.StartCommand
import core.interact.commands.attachBoardSequence
import core.interact.i18n.LanguageContainer
import core.interact.parse.NamedParser
import core.interact.parse.asParseFailure
import core.session.SessionManager
import dev.minn.jda.ktx.interactions.option
import dev.minn.jda.ktx.interactions.slash
import discord.assets.extractId
import discord.assets.extractUser
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
import utils.structs.asOption
import utils.structs.orElse

object StartCommandParser : NamedParser, ParsableCommand, EmbeddableCommand, BuildableCommand {

    override val name = "start"

    private suspend fun lookupRequestSent(context: InteractionContext<*>, owner: User): Option<DiscordParseFailure> =
        SessionManager.retrieveRequestSessionByOwner(context.bot.sessionRepository, context.config.id, owner.id)?.let { session ->
            Option(this.asParseFailure("already sent request session", owner) { producer, publisher, container ->
                producer.produceRequestAlreadySent(publisher, container, owner, session.opponent).map { it.launch(); Order.Unit }
            })
        } ?: Option.Empty

    private suspend fun lookupRequestOwner(context: InteractionContext<*>, owner: User): Option<DiscordParseFailure> =
        SessionManager.retrieveRequestSession(context.bot.sessionRepository, context.config.id, owner.id)?.let { session ->
            Option(this.asParseFailure("already has request session", owner) { producer, publisher, container ->
                producer.produceRequestAlready(publisher, container, session.owner, session.opponent).map { it.launch(); Order.Unit }
            })
        } ?: Option.Empty

    private suspend fun lookupRequestOpponent(context: InteractionContext<*>, owner: User, opponent: User): Option<DiscordParseFailure> =
        SessionManager.retrieveRequestSession(context.bot.sessionRepository, context.config.id, opponent.id)?.let { _ ->
            Option(this.asParseFailure("try to send request session but $opponent already has request session", owner) { producer, publisher, container ->
                producer.produceOpponentRequestAlready(publisher, container, owner, opponent).map { it.launch(); Order.Unit }
            })
        } ?: Option.Empty

    private suspend fun lookupSessionOwner(context: InteractionContext<*>, user: User): Option<DiscordParseFailure> =
        SessionManager.retrieveGameSession(context.bot.sessionRepository, context.config.id, user.id)?.let { session ->
            Option(this.asParseFailure("already has game session", user) { producer, publisher, container ->
                producer.produceSessionAlready(publisher, container, session.owner)
                    .map { SessionManager.appendMessage(context.bot.sessionRepository, session.messageBufferKey, it.retrieve().message) }
                    .attachBoardSequence(context.bot, context.config, producer, publisher, session)
                    .map { Order.Unit }
            })
        } ?: Option.Empty

    private suspend fun lookupSessionOpponent(context: InteractionContext<*>, user: User, opponent: User): Option<DiscordParseFailure> =
        SessionManager.retrieveGameSession(context.bot.sessionRepository, context.config.id, opponent.id)?.let { session ->
            Option(this.asParseFailure("try to send request session but $opponent already has game session", user) { producer, publisher, container ->
                producer.produceOpponentSessionAlready(publisher, container, session.owner, opponent).map { it.launch(); Order.Unit }
            })
        } ?: Option.Empty

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
        val owner = context.event.user.extractUser()
        val opponent = context.event.getOption(context.config.language.container.startCommandOptionOpponent())
            ?.let {
                val jdaUser = it.asUser
                if (jdaUser.isBot) null
                else jdaUser.extractUser()
            }
            .asOption()

        return this.parseActually(context, owner, opponent)
    }

    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>, payload: List<String>): Either<Command, DiscordParseFailure> {
        val owner = context.event.author.extractUser()
        val opponent = context.event.message.mentionedUsers
            .firstOrNull { !it.isBot && it.idLong != owner.id.idLong }
            ?.extractUser()
            .asOption()

        return this.parseActually(context, owner, opponent)
    }

    override suspend fun parseButton(context: InteractionContext<GenericComponentInteractionCreateEvent>): Option<Command> {
        val ownerId = context.event.user.extractId()

        val opponentId = context.event.componentId
            .drop(2)
            .toLongOrNull()
            ?.let { UserId(it) }
            ?: return Option.Empty

        return if (
            SessionManager.hasRequestSession(context.bot.sessionRepository, context.guild.id, ownerId, opponentId)
            || SessionManager.hasGameSession(context.bot.sessionRepository, context.guild.id, ownerId, opponentId)
        )
            Option.Empty
        else
            Option(StartCommand(opponent = context.jdaGuild.getMemberById(opponentId.idLong)!!.user.extractUser()))
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
