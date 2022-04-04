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
import discord.interact.message.DiscordMessageProducer
import discord.interact.parse.*
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.structs.Either
import utils.structs.Option

object StartCommandParser : NamedParser, ParsableCommand, BuildableCommand {

    override val name = "start"

    private suspend fun lookupSession(context: InteractionContext<*>, user: User): Option<DiscordParseFailure> =
        if (SessionManager.retrieveGameSession(context.botContext.sessionRepository, context.config.id, user.id) != null)
            Option.Some(this.asParseFailure("$user already has game session") { _, publisher, _ ->
                DiscordMessageProducer.produceLanguageGuide(publisher).map { it.launch(); Order.Unit }
            })
        else Option.Empty

    override suspend fun parseSlash(context: InteractionContext<SlashCommandInteractionEvent>) =
        this.lookupSession(context, context.event.user.extractUser()).fold(
            onEmpty = { Either.Left(
                StartCommand(
                    opponent = context.event.getOption(context.config.language.container.startCommandOptionOpponent())?.let {
                        val user = it.asUser
                        if (user.isBot) null
                        else user.extractUser()
                    }
                )
            ) },
            onDefined = { Either.Right(it) }
        )


    override suspend fun parseText(context: InteractionContext<MessageReceivedEvent>) =
        this.lookupSession(context, context.event.author.extractUser()).fold(
            onEmpty = { Either.Left(
                StartCommand(
                    opponent = context.event.message.mentionedUsers
                        .firstOrNull { !it.isBot && it.idLong != context.event.author.idLong }
                        ?.extractUser()
                )
            ) },
            onDefined = { Either.Right(it) }
        )

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
