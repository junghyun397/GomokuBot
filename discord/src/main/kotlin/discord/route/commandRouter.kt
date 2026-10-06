@file:Suppress("DuplicatedCode")

package discord.route

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.effect
import core.assets.UNICODE_ALARM_CLOCK
import core.assets.UNICODE_CONSTRUCTION
import core.database.repositories.AnnounceRepository
import core.interact.commands.*
import core.interact.i18n.LanguageContainer
import core.interact.message.AdaptivePublisherSet
import core.interact.message.AppMessage
import core.interact.message.MonoPublisherSet
import core.interact.message.NoticeLevel
import core.interact.parse.ParseFailure
import discord.ActionLogRecord
import discord.assets.*
import discord.executeAndRecord
import discord.interact.ChannelManager
import discord.interact.UserInteractionContext
import discord.interact.message.*
import discord.interact.parse.ParsableCommand
import discord.interact.parse.parsers.*
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import utils.replaceIf
import java.util.concurrent.TimeUnit

private fun buildPermissionNode(context: UserInteractionContext<*>, parsableCommand: ParsableCommand, channel: GuildMessageChannel, jdaUser: User): Either<ParseFailure, ParsableCommand> =
    ChannelManager.permissionDependedRun(
        channel, Permission.MESSAGE_SEND,
        onMissed = { Either.Left(
            ParseFailure(parsableCommand.name, "message permission not granted in $channel", context.channel, context.user) { _, _, container ->
                effect {
                    jdaUser.openPrivateChannel()
                        .flatMap { privateSubChannel ->
                            val message = AppMessage.Embed(
                                level = NoticeLevel.ERROR,
                                title = "$UNICODE_CONSTRUCTION ${container.somethingWrongEmbedTitle}",
                                description = container.permissionNotGrantedEmbedDescription("`${channel.name}`") +
                                    "\n\n$UNICODE_ALARM_CLOCK ${container.permissionNotGrantedEmbedFooter}",
                            )
                            privateSubChannel.sendMessage(DiscordMessageRenderer.render(message).buildCreate())
                        }
                        .delay(1, TimeUnit.MINUTES)
                        .flatMap(Message::delete)
                        .queue()
                }
            }
        ) },
        onGranted = { Either.Right(parsableCommand) }
    )

private fun <T : Event> buildAnnounceNode(context: UserInteractionContext<T>, command: Command): Command =
    command.replaceIf((context.user.announceId ?: -1) < (AnnounceRepository.getLatestAnnounceId(context.bot.dbConnection) ?: -1)) {
        AnnounceCommand(command)
    }

private fun <T : Event> buildUpdateProfileNode(context: UserInteractionContext<T>, jdaUser: User, command: Command): Command {
    val user = jdaUser.profile(uid = context.user.id, announceId = context.user.announceId)
    val channel = context.jdaChannel.profile(
        uid = context.channel.id,
        commandRevision = context.channel.commandRevision,
    )

    val maybeThenUser = if (user != context.user) user else null
    val maybeThenChannel = if (channel != context.channel) channel else null

    return command.replaceIf(maybeThenUser != null || maybeThenChannel != null) {
        UpdateProfileCommand(command, maybeThenUser, maybeThenChannel)
    }
}

private fun <T : Event> buildUpdateCommandsNode(context: UserInteractionContext<T>, command: Command): Command {
    if (context.channel.commandRevision >= Command.COMMAND_REVISION)
        return command

    return UpdateCommandsCommand(
        command = command,
        previousRevision = context.channel.commandRevision,
        targetRevision = Command.COMMAND_REVISION,
    )
}

private fun matchCommand(command: String, container: LanguageContainer): ParsableCommand? =
    when (command.lowercase()) {
        "help" -> HelpCommandParser
        container.helpCommand -> HelpCommandParser
        container.settingsCommand -> SettingsCommandParser
        container.startCommand -> StartCommandParser
        "s" -> SetCommandParser
        container.undoCommand -> UndoCommandParser
        container.resignCommand -> ResignCommandParser
        container.languageCommand -> LangCommandParser
        container.rankCommand -> RankCommandParser
        container.ratingCommand -> RatingCommandParser
        container.replayCommand -> ReplayListCommandParser
        container.boardCommand -> BoardCommandParser
        else -> null
    }

fun commandAutoCompleteRouter(event: CommandAutoCompleteInteractionEvent) {
    if (event.name != SetCommandParser.name)
        return

    // TODO
}

suspend fun slashCommandRouter(context: UserInteractionContext<SlashCommandInteractionEvent>): List<ActionLogRecord>? {
    val parsable = matchCommand(context.event.name, context.config.language.container)
        ?: return null
    val platform = DiscordPlatformService(context.shardManager, context.discordConfig, context.jdaChannel)

    val parsed: Either<ParseFailure, Command> = buildPermissionNode(
        context,
        parsable,
        context.event.channel.asGuildMessageChannel(),
        context.event.user
    )
        .flatMap { parsable.parseSlash(context) }
        .map { command ->
            buildAnnounceNode(
                context,
                buildUpdateProfileNode(
                    context,
                    context.event.user,
                    buildUpdateCommandsNode(context, command)
                )
            )
        }

    parsed.fold(
        ifLeft = { },
        ifRight = { command ->
            val responseFlag = command.responseFlag

            if (responseFlag is ResponseFlag.Defer)
                context.event.deferReply().setEphemeral(responseFlag.windowed).queue()
        }
    )

    val result = parsed.fold(
        ifRight = { command ->
            command.execute(
                bot = context.bot,
                config = context.config,
                channel = context.channel,
                user = context.user,
                service = platform,
                publishers = when (command.responseFlag) {
                    is ResponseFlag.Defer -> AdaptivePublisherSet(
                        plain = discordPublisher { msg -> MessageCreateAdaptor(context.event.hook.sendMessage(msg.buildCreate())) },
                        windowed = discordPublisher { msg -> MessageCreateAdaptor(context.event.hook.sendMessage(msg.buildCreate())) },
                        editGlobal = { ref -> discordPublisher { msg -> context.jdaChannel.editMessageByMessageRef(ref, msg.buildEdit()) } },
                    )
                    else -> TransMessagePublisherSet(
                        head = AdaptivePublisherSet(
                            plain = discordPublisher { msg -> WebHookMessageCreateAdaptor(context.event.reply(msg.buildCreate())) },
                            windowed = discordPublisher { msg -> WebHookMessageCreateAdaptor(context.event.reply(msg.buildCreate()).setEphemeral(true)) },
                        ),
                        tail = AdaptivePublisherSet(
                            plain = discordPublisher { msg -> MessageCreateAdaptor(context.event.hook.sendMessage(msg.buildCreate())) },
                            windowed = discordPublisher { msg -> MessageCreateAdaptor(context.event.hook.sendMessage(msg.buildCreate()).setEphemeral(true)) },
                            editGlobal = { ref -> discordPublisher { msg -> context.jdaChannel.editMessageByMessageRef(ref, msg.buildEdit()) } },
                        ),
                    )
                },
                emittedTime = context.emittedTime,
            )
        },
        ifLeft = { parseFailure ->
            parseFailure.notice(
                config = context.config,
                service = platform,
                publisher = TransMessagePublisher(
                    head = discordPublisher { msg -> WebHookMessageCreateAdaptor(context.event.reply(msg.buildCreate()).setEphemeral(true)) },
                    tail = discordPublisher { msg -> MessageCreateAdaptor(context.event.hook.sendMessage(msg.buildCreate()).setEphemeral(true)) }
                ),
                emittedTime = context.emittedTime,
            )
        }
    )

    return executeAndRecord(context, result)
}

suspend fun textCommandRouter(context: UserInteractionContext<MessageReceivedEvent>): List<ActionLogRecord>? {
    val platform = DiscordPlatformService(context.shardManager, context.discordConfig, context.jdaChannel)
    val messageRaw = context.event.message.contentRaw

    val payload = when {
        messageRaw.startsWith(COMMAND_PREFIX) -> messageRaw.drop(1).split(" ")
        else -> {
            // drop <@000000000000000000>
            val payload = messageRaw.drop(21).trimStart().split(" ")

            when {
                payload.first().isEmpty() -> listOf("help")
                else -> payload
            }
        }
    }

    val parsable = matchCommand(command = payload.first(), container = context.config.language.container)
        ?: return null

    val parsed: Either<ParseFailure, Command> = buildPermissionNode(
        context,
        parsable,
        context.event.channel.asGuildMessageChannel(),
        context.event.author
    )
        .flatMap { parsable.parseText(context, payload) }
        .map { command ->
            buildAnnounceNode(
                context,
                buildUpdateProfileNode(
                    context,
                    context.event.author,
                    buildUpdateCommandsNode(context, command)
                )
            )
        }

    ChannelManager.permissionGrantedRun(context.event.channel.asGuildMessageChannel(), Permission.MESSAGE_ADD_REACTION) {
        parsed.fold(
            ifRight = { context.event.message.addReaction(EMOJI_CHECK).queue() },
            ifLeft = { context.event.message.addReaction(EMOJI_CROSS).queue() },
        )
    }

    val result = parsed.fold(
        ifRight = { command ->
            command.execute(
                bot = context.bot,
                config = context.config,
                channel = context.channel,
                user = context.user,
                service = platform,
                publishers = MonoPublisherSet(
                    publisher = discordPublisher { msg -> MessageCreateAdaptor(context.event.message.reply(msg.buildCreate())) },
                    editGlobal = { ref -> discordPublisher { msg -> context.jdaChannel.editMessageByMessageRef(ref, msg.buildEdit()) } },
                ),
                emittedTime = context.emittedTime,
            )
        },
        ifLeft = { parseFailure ->
            parseFailure.notice(
                config = context.config,
                service = platform,
                publisher = discordPublisher { msg -> MessageCreateAdaptor(context.event.message.reply(msg.buildCreate())) },
                emittedTime = context.emittedTime,
            )
        }
    )

    return executeAndRecord(context, result)
}
