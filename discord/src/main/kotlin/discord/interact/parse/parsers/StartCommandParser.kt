package discord.interact.parse.parsers

import core.interact.Order
import core.assets.User
import dev.minn.jda.ktx.interactions.option
import dev.minn.jda.ktx.interactions.slash
import core.interact.commands.StartCommand
import core.interact.i18n.LanguageContainer
import core.interact.parse.NamedParser
import core.interact.parse.asParseFailure
import core.session.SessionManager
import discord.assets.extractUser
import discord.interact.InteractionContext
import discord.interact.parse.*
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.structs.Either
import utils.structs.Option
import utils.structs.orElse

object StartCommandParser : NamedParser, ParsableCommand, BuildableCommand {

    override val name = "start"

    private suspend fun lookupRequest(context: InteractionContext<*>, user: User): Option<DiscordParseFailure> =
        SessionManager.retrieveRequestSession(context.botContext.sessionRepository, context.config.id, user.id)?.let { session ->
            Option.Some(this.asParseFailure("already has request session", user) { producer, publisher, container ->
                producer.produceRequestAlready(publisher, container, session.owner).map { it.launch(); Order.Unit }
            })
        } ?: Option.Empty

    private suspend fun lookupSession(context: InteractionContext<*>, user: User): Option<DiscordParseFailure> =
        SessionManager.retrieveGameSession(context.botContext.sessionRepository, context.config.id, user.id)?.let { session ->
            Option.Some(this.asParseFailure("already has game session", user) { producer, publisher, container ->
                producer.produceSessionAlready(publisher, container, session.owner).map { it.launch(); Order.Unit }
            })
        } ?: Option.Empty

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>) =
        context.event.user.extractUser().let { user ->
            this.lookupSession(context, user)
                .orElse { this.lookupRequest(context, user) }
                .fold(
                    onEmpty = { Either.Left(
                        StartCommand(
                            opponent = context.event.getOption(context.config.language.container.startCommandOptionOpponent())
                                ?.let {
                                    val jdaUser = it.asUser
                                    if (jdaUser.isBot) null
                                    else jdaUser.extractUser()
                                }
                        )
                    ) },
                    onDefined = { Either.Right(it) }
                )
        }


    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>) =
        context.event.author.extractUser().let { user ->
        this.lookupSession(context, user)
            .orElse { this.lookupSession(context, user) }
            .fold(
                onEmpty = { Either.Left(
                    StartCommand(
                        opponent = context.event.message.mentionedUsers
                            .firstOrNull { !it.isBot && it.idLong != user.id.idLong }
                            ?.extractUser()
                    )
                ) },
                onDefined = { Either.Right(it) }
            )
        }

    override fun buildCommandData(action: CommandListUpdateAction, languageContainer: LanguageContainer) =
        action.slash(
            languageContainer.startCommand(),
            languageContainer.startCommandDescription()
        ) {
            option<net.dv8tion.jda.api.entities.User>(
                languageContainer.startCommandOptionOpponent(),
                languageContainer.startCommandOptionOpponentDescription(),
                false
            )
        }

}
