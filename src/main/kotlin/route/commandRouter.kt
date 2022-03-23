package route

import interact.GuildManager
import interact.commands.ParsableCommand
import interact.commands.entities.*
import interact.i18n.LanguageContainer
import interact.reports.CommandReport
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple2
import utility.*

private fun matchCommand(command: String, languageContainer: LanguageContainer): Option<ParsableCommand> =
    when (command.lowercase()) {
        languageContainer.helpCommand() -> Option.Some(HelpCommand)
        languageContainer.startCommand() -> Option.Some(StartCommand)
        "s" -> Option.Some(SetCommand)
        languageContainer.resignCommand() -> Option.Some(ResignCommand)
        languageContainer.langCommand() -> Option.Some(LangCommand)
        languageContainer.styleCommand() -> Option.Some(StyleCommand)
        languageContainer.rankCommand() -> Option.Some(RankCommand)
        languageContainer.ratingCommand() -> Option.Some(RatingCommand)
        else -> Option.Empty
    }

fun slashCommandRouter(context: InteractionContext<SlashCommandInteractionEvent>): Mono<Tuple2<InteractionContext<SlashCommandInteractionEvent>, Result<CommandReport>>> =
    Mono.zip(
        context.toMono(),
        matchCommand(
            command = context.event.name,
            languageContainer = context.guildConfig.language.container
        ).toMono()
    )
        .filter { it.t2.isDefined }
        .doOnNext { it.t1.event
            .deferReply().queue()
        }
        .flatMap { Mono.zip(it.t1.toMono(), it.t2.getOrNull()!!
            .parse(event = it.t1.event, languageContainer = it.t1.guildConfig.language.container).toMono()
        ) }
        .flatMap { Mono.zip(it.t1.toMono(), mono { it.t2.fold(
            onLeft = { command ->
                command.execute(
                    botContext = it.t1.botContext,
                    guildConfig = it.t1.guildConfig,
                    userId = it.t1.event.user.extractId(),
                ) { msg -> GuildManager.permissionSafeRun(it.t1.event.guildChannel, Permission.MESSAGE_SEND) { _ ->
                    WebHookRestActionAdaptor(it.t1.event.hook.sendMessage(msg))
                } }
            } ,
            onRight = { parseFailure ->
                parseFailure.notice(
                    guildConfig = it.t1.guildConfig,
                ) { msg -> GuildManager.permissionSafeRun(it.t1.event.guildChannel, Permission.MESSAGE_SEND) { _ ->
                    WebHookRestActionAdaptor(it.t1.event.hook.sendMessage(msg))
                } }
            }
        ) }) }

fun textCommandRouter(context: InteractionContext<MessageReceivedEvent>): Mono<Tuple2<InteractionContext<MessageReceivedEvent>, Result<CommandReport>>> =
    Mono.zip(
        context.toMono(),
        matchCommand(
            command = context.event.message.contentRaw.split(" ")[0].substring(1),
            languageContainer = context.guildConfig.language.container
        ).toMono()
    )
        .filter { it.t2.isDefined }
        .flatMap { Mono.zip(it.t1.toMono(), it.t2.getOrNull()!!
            .parse(event = it.t1.event, languageContainer = it.t1.guildConfig.language.container).toMono()
        ) }
        .doOnNext {
            GuildManager.permissionSafeRun(it.t1.event.guildChannel, Permission.MESSAGE_ADD_REACTION) { _ ->
                if (it.t2.isLeft) it.t1.event.message.addReaction(UNICODE_CHECK).queue()
                else it.t1.event.message.addReaction(UNICODE_CROSS).queue()
            }
        }
        .flatMap { Mono.zip(it.t1.toMono(), mono { it.t2.fold(
            onLeft = { command ->
                command.execute(
                    botContext = it.t1.botContext,
                    guildConfig = it.t1.guildConfig,
                    userId =it.t1.event.author.extractId(),
                ) { msg -> GuildManager.permissionSafeRun(it.t1.event.guildChannel, Permission.MESSAGE_SEND) { _ ->
                    MessageActionRestActionAdaptor(it.t1.event.message.reply(msg))
                } }
            },
            onRight = { parseFailure ->
                parseFailure.notice(
                    guildConfig = it.t1.guildConfig
                ) { msg -> GuildManager.permissionSafeRun(it.t1.event.guildChannel, Permission.MESSAGE_SEND) { _ ->
                    MessageActionRestActionAdaptor(it.t1.event.message.reply(msg))
                } }
            }
        ) }) }
