package discord.interact.parse.parsers

import arrow.core.Either
import arrow.core.raise.effect
import core.assets.User
import core.database.repositories.UserProfileRepository
import core.interact.commands.Command
import core.interact.commands.StartCommand
import core.interact.commands.buildBoardProcedure
import core.interact.i18n.LanguageContainer
import core.interact.parse.CommandParser
import core.interact.parse.ParseFailure
import core.interact.parse.asParseFailure
import core.session.MessageManager
import core.session.SessionManager
import core.session.entities.GameSession
import core.session.entities.RequestSession
import core.session.entities.Rule
import core.session.entities.SwapType
import dev.minn.jda.ktx.interactions.commands.choice
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.slash
import discord.assets.COMMAND_PREFIX
import discord.assets.DISCORD_PLATFORM_ID
import discord.assets.extractId
import discord.assets.extractProfile
import discord.interact.UserInteractionContext
import discord.interact.parse.BuildableCommand
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
            description = container.commandUsageStartEngine()
        ),
        BuildableCommand.Usage(
            usage = "``/${container.startCommand()} @mention`` or ``$COMMAND_PREFIX${container.startCommand()} @mention``",
            description = container.commandUsageStartPvp()
        ),
    )

    private fun ruleFromString(container: LanguageContainer, source: String?): Rule =
        when (source) {
            container.ruleSelectTaraguchi10() -> Rule.TARAGUCHI_10
            container.ruleSelectSoosyrv8() -> Rule.SOOSYRV_8
            else -> Rule.RENJU
        }

    private fun findRequestSession(context: UserInteractionContext<*>, user: User.Human): RequestSession? {
        val sessionId = SessionManager.findRequestSessionId(context.bot.sessions, context.channel.id, user.id)
            ?: return null

        return SessionManager.retrieveRequestSession(context.bot.sessions, sessionId).snapshot()
    }

    private fun findGameSession(context: UserInteractionContext<*>, user: User.Human): GameSession? {
        val sessionId = SessionManager.findGameSessionId(context.bot.sessions, context.channel.id, user.id)
            ?: return null

        return SessionManager.retrieveGameSession(context.bot.sessions, sessionId).snapshot()
    }

    private fun lookupRequestSent(context: UserInteractionContext<*>, requester: User.Human): ParseFailure? =
        this.findRequestSession(context, requester)
            ?.takeIf { it.requester.id == requester.id }
            ?.let { session ->
                    this.asParseFailure("already sent request session", context.channel, requester) { messagingService, publisher, container ->
                        effect {
                            messagingService.buildRequestAlreadySent(publisher, container, session.opponent)
                                .launch()()
                            emptyList()
                        }
                    }
                }

    private fun lookupRequestParticipant(context: UserInteractionContext<*>, requester: User.Human): ParseFailure? =
        this.findRequestSession(context, requester)
            ?.let { session ->
                    this.asParseFailure("already has request session", context.channel, requester) { messagingService, publisher, container ->
                        effect {
                            messagingService.buildRequestAlready(publisher, container, session.requester)
                                .launch()()
                            emptyList()
                        }
                    }
                }

    private fun lookupRequestOpponent(context: UserInteractionContext<*>, requester: User.Human, opponent: User.Human): ParseFailure? =
        this.findRequestSession(context, opponent)
            ?.let {
                    this.asParseFailure("try to send request session but $opponent already has request session", context.channel, requester) { messagingService, publisher, container ->
                        effect {
                            messagingService.buildOpponentRequestAlready(publisher, container, opponent)
                                .launch()()
                            emptyList()
                        }
                    }
                }

    private fun lookupExistingGameSession(context: UserInteractionContext<*>, user: User.Human): ParseFailure? =
        this.findGameSession(context, user)
            ?.let { session ->
                    this.asParseFailure("already has game session", context.channel, user) { messagingService, publisher, container ->
                        effect {
                            val message = messagingService.buildSessionAlready(publisher, container)
                                .retrieve()()

                            message?.let { MessageManager.appendMessage(context.bot.sessions, session.messageBufferKey, it.ref) }

                            when (context.config.swapType) {
                                SwapType.EDIT -> Unit
                                else -> buildBoardProcedure(context.bot, context.config, messagingService, publisher, session)()
                            }

                            emptyList()
                        }
                    }
                }

    private fun lookupOpponentGameSession(context: UserInteractionContext<*>, user: User.Human, opponent: User.Human): ParseFailure? =
        this.findGameSession(context, opponent)
            ?.let {
                    this.asParseFailure("try to send request session but $opponent already has game session", context.channel, user) { messagingService, publisher, container ->
                        effect {
                            messagingService.buildOpponentSessionAlready(publisher, container, opponent)
                                .launch()()
                            emptyList()
                        }
                    }
                }

    private fun parseActually(context: UserInteractionContext<*>, requester: User.Human, opponent: User.Human?, rule: Rule): Either<ParseFailure, Command> {
        this.lookupExistingGameSession(context, requester)?.let { failure ->
            return Either.Left(failure)
        }

        if (opponent != null) {
            this.lookupOpponentGameSession(context, requester, opponent)?.let { failure ->
                return Either.Left(failure)
            }
        }

        this.lookupRequestSent(context, requester)?.let { failure ->
            return Either.Left(failure)
        }

        this.lookupRequestParticipant(context, requester)?.let { failure ->
            return Either.Left(failure)
        }

        if (opponent != null) {
            this.lookupRequestOpponent(context, requester, opponent)?.let { failure ->
                return Either.Left(failure)
            }
        }

        return Either.Right(
            StartCommand(
                opponent = opponent ?: User.GomokuBot,
                rule = rule
            )
        )
    }

    override suspend fun parseSlash(context: UserInteractionContext<SlashCommandInteractionEvent>): Either<ParseFailure, Command> {
        val requester = context.user
        val jdaUser = context.event.getOption(context.config.language.container.startCommandOptionOpponent())?.asUser
        val opponent = if (jdaUser != null && !jdaUser.isBot)
            UserProfileRepository.retrieveOrInsertUser(context.bot.dbConnection, DISCORD_PLATFORM_ID, jdaUser.extractId()) {
                jdaUser.extractProfile()
            }
        else
            null

        val rule = this.ruleFromString(
            context.config.language.container,
            context.event.getOption(context.config.language.container.startCommandOptionRule())?.asString
        )

        return this.parseActually(context, requester, opponent, rule)
    }

    override suspend fun parseText(context: UserInteractionContext<MessageReceivedEvent>, payload: List<String>): Either<ParseFailure, Command> {
        val requester = context.user
        val opponent = context.event.message.mentions.members
            .firstOrNull { !it.user.isBot && it.idLong != requester.givenId.idLong }
            ?.user
            ?.let {
                UserProfileRepository.retrieveOrInsertUser(context.bot.dbConnection, DISCORD_PLATFORM_ID, it.extractId()) {
                    it.extractProfile()
                }
            }

        val rule = this.ruleFromString(
            context.config.language.container,
            payload.getOrNull(2)?.lowercase()
        )

        return this.parseActually(context, requester, opponent, rule)
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
