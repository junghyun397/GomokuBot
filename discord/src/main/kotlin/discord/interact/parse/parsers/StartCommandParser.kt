package discord.interact.parse.parsers

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

    private suspend fun lookupRequestSent(context: UserInteractionContext<*>, owner: User): Option<DiscordParseFailure> =
        SessionManager.retrieveRequestSessionByOwner(context.bot.sessions, context.guild, owner.id)
            .toOption()
            .fold(
                ifSome = { session ->
                    Some(this.asParseFailure("already sent request session", context.guild, owner) { messagingService, publisher, container ->
                        effect {
                            messagingService.buildRequestAlreadySent(publisher, container, session.opponent)
                                .launch()()
                            emptyList()
                        }
                    })
                },
                ifEmpty = { None }
            )

    private suspend fun lookupRequestOwner(context: UserInteractionContext<*>, owner: User): Option<DiscordParseFailure> =
        SessionManager.retrieveRequestSession(context.bot.sessions, context.guild, owner.id)
            .toOption()
            .fold(
                ifSome = { session ->
                    Some(this.asParseFailure("already has request session", context.guild, owner) { messagingService, publisher, container ->
                        effect {
                            messagingService.buildRequestAlready(publisher, container, session.owner)
                                .launch()()
                            emptyList()
                        }
                    })
                },
                ifEmpty = { None }
            )

    private suspend fun lookupRequestOpponent(context: UserInteractionContext<*>, owner: User, opponent: User): Option<DiscordParseFailure> =
        SessionManager.retrieveRequestSession(context.bot.sessions, context.guild, opponent.id)
            .toOption()
            .fold(
                ifSome = {
                    Some(this.asParseFailure("try to send request session but $opponent already has request session", context.guild, owner) { messagingService, publisher, container ->
                        effect {
                            messagingService.buildOpponentRequestAlready(publisher, container, opponent)
                                .launch()()
                            emptyList()
                        }
                    })
                },
                ifEmpty = { None }
            )

    private suspend fun lookupSessionOwner(context: UserInteractionContext<*>, user: User): Option<DiscordParseFailure> =
        SessionManager.retrieveGameSession(context.bot.sessions, context.guild, user.id)
            .toOption()
            .fold(
                ifSome = { session ->
                    Some(this.asParseFailure("already has game session", context.guild, user) { messagingService, publisher, container ->
                        effect {
                            val maybeMessage = messagingService.buildSessionAlready(publisher, container)
                                .retrieve()()

                            maybeMessage.fold(
                                ifSome = { SessionManager.appendMessage(context.bot.sessions, session.messageBufferKey, it.messageRef) },
                                ifEmpty = { }
                            )

                            when (context.config.swapType) {
                                SwapType.EDIT -> Unit
                                else -> buildBoardProcedure(context.bot, context.guild, context.config, messagingService, publisher, session)()
                            }

                            emptyList()
                        }
                    })
                },
                ifEmpty = { None }
            )

    private suspend fun lookupSessionOpponent(context: UserInteractionContext<*>, user: User, opponent: User): Option<DiscordParseFailure> =
        SessionManager.retrieveGameSession(context.bot.sessions, context.guild, opponent.id)
            .toOption()
            .fold(
                ifSome = {
                    Some(this.asParseFailure("try to send request session but $opponent already has game session", context.guild, user) { messagingService, publisher, container ->
                        effect {
                            messagingService.buildOpponentSessionAlready(publisher, container, opponent)
                                .launch()()
                            emptyList()
                        }
                    })
                },
                ifEmpty = { None }
            )

    private suspend fun parseActually(context: UserInteractionContext<*>, owner: User, opponent: Option<User>, rule: Rule): Either<DiscordParseFailure, Command> {
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
                    None -> null
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
