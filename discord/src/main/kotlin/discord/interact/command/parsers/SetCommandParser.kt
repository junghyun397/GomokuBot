package discord.interact.command.parsers

import dev.minn.jda.ktx.interactions.option
import dev.minn.jda.ktx.interactions.slash
import discord.interact.command.ParseFailure
import core.interact.commands.Command
import core.interact.i18n.LanguageContainer
import discord.interact.command.BuildableCommand
import discord.interact.command.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.monads.Either

object SetCommandParser : ParsableCommand, BuildableCommand {

    override val name = "s"

    override fun parse(event: SlashCommandInteractionEvent, languageContainer: LanguageContainer): Either<Command, ParseFailure> {
        TODO("Not yet implemented")
    }

    override fun parse(event: MessageReceivedEvent, languageContainer: LanguageContainer): Either<Command, ParseFailure> {
        TODO("Not yet implemented")
    }

    override fun buildCommandData(action: CommandListUpdateAction, languageContainer: LanguageContainer) =
        action.slash(
            "s",
            "set"
        ) {
            option<String>("row", "alphabet", true)
            option<Int>("column", "number", true)
        }

}
