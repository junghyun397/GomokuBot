package discord.interact.command

import core.interact.commands.Command
import core.interact.i18n.LanguageContainer
import discord.interact.message.DiscordMessagePublisher
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utils.monads.Either
import utils.monads.IO

interface ParsableCommand {

    val name: String

    fun parse(event: SlashCommandInteractionEvent, languageContainer: LanguageContainer): Either<Command, ParseFailure>

    fun parse(event: MessageReceivedEvent, languageContainer: LanguageContainer): Either<Command, ParseFailure>

}

fun ParsableCommand.asParseFailure(comment: String, onFailure: (LanguageContainer, DiscordMessagePublisher) -> IO<Unit>) =
    ParseFailure(this.name, comment, onFailure)
