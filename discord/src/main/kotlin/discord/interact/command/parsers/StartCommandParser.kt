package discord.interact.command.parsers

import dev.minn.jda.ktx.interactions.option
import dev.minn.jda.ktx.interactions.slash
import core.interact.commands.StartCommand
import core.interact.i18n.LanguageContainer
import discord.interact.command.BuildableCommand
import discord.interact.command.ParsableCommand
import discord.assets.extractId
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.monads.Either

object StartCommandParser : ParsableCommand, BuildableCommand {

    override val name = "start"

    override fun parse(event: SlashCommandInteractionEvent, languageContainer: LanguageContainer) =
        Either.Left(
            StartCommand(
                opponent = event.getOption(languageContainer.startCommandOptionOpponent())?.let {
                    val user = it.asUser
                    if (user.isBot) null
                    else user.extractId()
                }
            )
        )

    override fun parse(event: MessageReceivedEvent, languageContainer: LanguageContainer) =
        Either.Left(
            StartCommand(
                opponent = event.message.mentionedUsers
                    .firstOrNull { !it.isBot && it.idLong != event.author.idLong }
                    ?.extractId()
            )
        )

    override fun buildCommandData(action: CommandListUpdateAction, languageContainer: LanguageContainer) =
        action.slash(
            languageContainer.startCommand(),
            languageContainer.startCommandDescription()
        ) {
            option<User>(
                languageContainer.startCommandOptionOpponent(),
                languageContainer.startCommandOptionOpponentDescription(),
                false
            )
        }

}
