package discord.interact.parse.parsers

import core.session.MessageManager
import arrow.core.*
import arrow.core.raise.effect
import core.assets.User
import core.database.repositories.UserProfileRepository
import core.interact.commands.Command
import core.interact.commands.StartCommand
import core.interact.commands.buildBoardProcedure
import core.interact.i18n.LanguageContainer
import core.interact.parse.CommandParser
import core.interact.parse.asParseFailure
import core.session.Rule
import core.session.SessionManager
import core.session.SwapType
import core.session.entities.GameSession
import core.session.entities.RequestSession
import dev.minn.jda.ktx.interactions.commands.choice
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.slash
import discord.assets.COMMAND_PREFIX
import discord.assets.DISCORD_PLATFORM_ID
import discord.assets.extractId
import discord.assets.extractProfile
import discord.interact.UserInteractionContext
import discord.interact.parse.BuildableCommand
import discord.interact.parse.DiscordParseFailure
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction

object StartCommandParser : CommandParser, ParsableCommand, BuildableCommand {

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

    private fun ruleFromString(container: LanguageContainer, source: String?): Rule =
        when (source) {
            container.ruleSelectTaraguchi10() -> Rule.TARAGUCHI_10
            container.ruleSelectSoosyrv8() -> Rule.SOOSYRV_8
            else -> Rule.RENJU
        }

    private suspend fun findRequestSession(context: UserInteractionContext<*>, user: User.Human): RequestSession? {
        val sessionId = SessionManager.findRequestSessionId(context.bot.sessions, context.channel.id, user.id)
            ?: return null

        return SessionManager.retrieveRequestSession(context.bot.sessions, sessionId).snapshot()
    }

    private suspend fun findGameSession(context: UserInteractionContext<*>, user: User.Human): GameSession? {
        val sessionId = SessionManager.findGameSessionId(context.bot.sessions, context.channel.id, user.id)
            ?: return null

        return SessionManager.retrieveGameSession(context.bot.sessions, sessionId).snapshot()
    }

    private suspend fun lookupRequestSent(context: UserInteractionContext<*>, owner: User.Human): Option<DiscordParseFailure> =
        this.findRequestSession(context, owner)
            ?.takeIf { it.owner.id == owner.id }
            .toOption()
            .fold(
                ifSome = { session ->
                    Some(this.asParseFailure("already sent request session", context.channel, owner) { messagingService, publisher, container ->
                        effect {
                            messagingService.buildRequestAlreadySent(publisher, container, session.opponent)
                                .launch()()
                            emptyList()
                        }
                    })
                },
                ifEmpty = { None }
            )

    private suspend fun lookupRequestOwner(context: UserInteractionContext<*>, owner: User.Human): Option<DiscordParseFailure> =
        this.findRequestSession(context, owner)
            .toOption()
            .fold(
                ifSome = { session ->
                    Some(this.asParseFailure("already has request session", context.channel, owner) { messagingService, publisher, container ->
                        effect {
                            messagingService.buildRequestAlready(publisher, container, session.owner)
                                .launch()()
                            emptyList()
                        }
                    })
                },
                ifEmpty = { None }
            )

    private suspend fun lookupRequestOpponent(context: UserInteractionContext<*>, owner: User.Human, opponent: User.Human): Option<DiscordParseFailure> =
        this.findRequestSession(context, opponent)
            .toOption()
            .fold(
                ifSome = {
                    Some(this.asParseFailure("try to send request session but $opponent already has request session", context.channel, owner) { messagingService, publisher, container ->
                        effect {
                            messagingService.buildOpponentRequestAlready(publisher, container, opponent)
                                .launch()()
                            emptyList()
                        }
                    })
                },
                ifEmpty = { None }
            )

    private suspend fun lookupSessionOwner(context: UserInteractionContext<*>, user: User.Human): Option<DiscordParseFailure> =
        this.findGameSession(context, user)
            .toOption()
            .fold(
                ifSome = { session ->
                    Some(this.asParseFailure("already has game session", context.channel, user) { messagingService, publisher, container ->
                        effect {
                            val maybeMessage = messagingService.buildSessionAlready(publisher, container)
                                .retrieve()()

                            maybeMessage.fold(
                                ifSome = { MessageManager.appendMessage(context.bot.sessions, session.messageBufferKey, it.messageRef) },
                                ifEmpty = { }
                            )

                            when (context.config.swapType) {
                                SwapType.EDIT -> Unit
                                else -> buildBoardProcedure(context.bot, context.channel, context.config, messagingService, publisher, session)()
                            }

                            emptyList()
                        }
                    })
                },
                ifEmpty = { None }
            )

    private suspend fun lookupSessionOpponent(context: UserInteractionContext<*>, user: User.Human, opponent: User.Human): Option<DiscordParseFailure> =
        this.findGameSession(context, opponent)
            .toOption()
            .fold(
                ifSome = {
                    Some(this.asParseFailure("try to send request session but $opponent already has game session", context.channel, user) { messagingService, publisher, container ->
                        effect {
                            messagingService.buildOpponentSessionAlready(publisher, container, opponent)
                                .launch()()
                            emptyList()
                        }
                    })
                },
                ifEmpty = { None }
            )

    private suspend fun parseActually(context: UserInteractionContext<*>, owner: User.Human, opponent: Option<User.Human>, rule: Rule): Either<DiscordParseFailure, Command> {
        when (val failure = this.lookupSessionOwner(context, owner)) {
            is Some -> return Either.Left(failure.value)
            None -> Unit
        }

        if (opponent is Some) {
            when (val failure = this.lookupSessionOpponent(context, owner, opponent.value)) {
                is Some -> return Either.Left(failure.value)
                None -> Unit
            }
        }

        when (val failure = this.lookupRequestSent(context, owner)) {
            is Some -> return Either.Left(failure.value)
            None -> Unit
        }

        when (val failure = this.lookupRequestOwner(context, owner)) {
            is Some -> return Either.Left(failure.value)
            None -> Unit
        }

        if (opponent is Some) {
            when (val failure = this.lookupRequestOpponent(context, owner, opponent.value)) {
                is Some -> return Either.Left(failure.value)
                None -> Unit
            }
        }

        return Either.Right(
            StartCommand(
                opponent = when (opponent) {
                    is Some -> opponent.value
                    None -> User.GomokuBot
                },
                rule = rule
            )
        )
    }

    override suspend fun parseSlash(context: UserInteractionContext<SlashCommandInteractionEvent>): Either<DiscordParseFailure, Command> {
        val owner = context.user
        val opponent = when (val option = context.event.getOption(context.config.language.container.startCommandOptionOpponent()).toOption()) {
            is Some -> {
                val jdaUser = option.value.asUser

                if (!jdaUser.isBot)
                    Some(
                        UserProfileRepository.retrieveOrInsertUser(context.bot.dbConnection, DISCORD_PLATFORM_ID, jdaUser.extractId()) {
                            jdaUser.extractProfile()
                        }
                    )
                else
                    None
            }
            None -> None
        }

        val rule = this.ruleFromString(
            context.config.language.container,
            context.event.getOption(context.config.language.container.startCommandOptionRule())?.asString
        )

        return this.parseActually(context, owner, opponent, rule)
    }

    override suspend fun parseText(context: UserInteractionContext<MessageReceivedEvent>, payload: List<String>): Either<DiscordParseFailure, Command> {
        val owner = context.user
        val opponent = context.event.message.mentions.members
            .firstOrNull { !it.user.isBot && it.idLong != owner.givenId.idLong }
            ?.user
            .toOption()
            .fold(
                ifSome = {
                    Some(UserProfileRepository.retrieveOrInsertUser(context.bot.dbConnection, DISCORD_PLATFORM_ID, it.extractId()) {
                        it.extractProfile()
                    })
                },
                ifEmpty = { None }
            )

        val rule = this.ruleFromString(
            context.config.language.container,
            payload.getOrNull(2)?.lowercase()
        )

        return this.parseActually(context, owner, opponent, rule)
    }

    override fun buildCommandData(action: CommandListUpdateAction, container: LanguageContainer) =
        action.slash(
            container.startCommand(),
            container.startCommandDescription()
        ) {
            option<net.dv8tion.jda.api.entities.User>(
                container.startCommandOptionOpponent(),
                container.startCommandOptionOpponentDescription(),
                required = false
            )
            option<String>(
                container.startCommandOptionRule(),
                container.startCommandOptionRuleDescription(),
                required = false
            ) {
                choice(container.ruleSelectRenju(), container.ruleSelectRenju())
                choice(container.ruleSelectTaraguchi10(), container.ruleSelectTaraguchi10())
                choice(container.ruleSelectSoosyrv8(), container.ruleSelectSoosyrv8())
            }
        }

}
