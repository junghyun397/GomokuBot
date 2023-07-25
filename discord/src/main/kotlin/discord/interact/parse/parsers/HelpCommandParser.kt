package discord.interact.parse.parsers

import core.interact.commands.Command
import core.interact.commands.HelpCommand
import core.interact.commands.ViewAnnounceCommand
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.interact.message.MessagingServiceImpl
import core.interact.parse.CommandParser
import dev.minn.jda.ktx.interactions.commands.choice
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.slash
import discord.assets.COMMAND_PREFIX
import discord.interact.UserInteractionContext
import discord.interact.parse.BuildableCommand
import discord.interact.parse.ParsableCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import utils.lang.shift
import utils.structs.Either
import utils.structs.fold

object HelpCommandParser : CommandParser, ParsableCommand, BuildableCommand {

    override val name = "help"

    override fun getLocalizedName(container: LanguageContainer) = container.helpCommand()

    override fun getLocalizedUsages(container: LanguageContainer) = listOf(
        BuildableCommand.Usage(
            usage = "``/${container.helpCommand()}`` or ``$COMMAND_PREFIX${container.helpCommand()}``",
            description = container.commandUsageHelp()
        )
    )

    private fun matchPage(container: LanguageContainer, shortcut: String?): Either<Unit, Int> =
        shortcut?.let { validShortcut ->
            if (validShortcut == container.helpCommandOptionAnnouncements() || validShortcut == Language.ENG.container.helpCommandOptionAnnouncements())
                Either.Left(Unit)
            else Either.Right(
                MessagingServiceImpl.aboutRenjuDocument[container]!!.second[validShortcut]
                    ?: MessagingServiceImpl.aboutRenjuDocument[Language.ENG.container]!!.second[validShortcut]
                    ?: 0
            )
        } ?: Either.Right(0)


    private fun checkCrossLanguageCommand(container: LanguageContainer, command: String) =
        container.helpCommand() != command

    override suspend fun parseSlash(context: UserInteractionContext<SlashCommandInteractionEvent>): Either.Left<Command> {
        val isCrossLanguageCommand = this.checkCrossLanguageCommand(context.config.language.container, context.event.name.lowercase())

        val language =
            if (isCrossLanguageCommand) Language.ENG
            else context.config.language

        return Either.Left(
            this.matchPage(language.container, context.event.getOption(language.container.helpCommandOptionShortcut())?.asString)
                .fold(
                    onLeft = { ViewAnnounceCommand(language) },
                    onRight = { page ->
                        HelpCommand(
                            sendSettings = isCrossLanguageCommand,
                            page = page
                        )
                    }
                )
        )
    }

    override suspend fun parseText(context: UserInteractionContext<MessageReceivedEvent>, payload: List<String>): Either.Left<Command> {
        val isCrossLanguageCommand = this.checkCrossLanguageCommand(context.config.language.container, payload[0].lowercase())

        val language =
            if (isCrossLanguageCommand) Language.ENG
            else context.config.language

        return Either.Left(
            this.matchPage(language.container, payload.getOrNull(1))
                .fold(
                    onLeft = { ViewAnnounceCommand(language) },
                    onRight = { page ->
                        HelpCommand(
                            sendSettings = isCrossLanguageCommand,
                            page = page
                        )
                    }
                )
        )
    }

    fun buildHelpCommandData(action: CommandListUpdateAction, container: LanguageContainer): CommandListUpdateAction =
        action.slash(
            container.helpCommand(),
            container.helpCommandDescription()
        ) {
            option<String>(container.helpCommandOptionShortcut(), container.helpCommandOptionShortcutDescription()) {
                MessagingServiceImpl.aboutRenjuDocument[container]!!.second.forEach { (anchor, _) ->
                    choice(anchor, anchor)
                }

                choice(container.helpCommandOptionAnnouncements(), container.helpCommandOptionAnnouncements())
            }
        }

    override fun buildCommandData(action: CommandListUpdateAction, container: LanguageContainer) =
        action.shift(container != Language.ENG.container) {
            this.buildHelpCommandData(action, container)
        }

}
