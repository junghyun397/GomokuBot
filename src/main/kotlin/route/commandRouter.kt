package route

import interact.commands.ParsableCommand
import interact.commands.entities.HelpCommand
import interact.commands.entities.StartCommand
import interact.i18n.LanguageContainer
import interact.reports.CommandReport
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import utility.MessageActionRestActionAdaptor
import utility.UserId
import utility.WebHookRestActionAdaptor

private fun matchCommand(command: String, languageContainer: LanguageContainer): Result<ParsableCommand> =
    when (command) {
        languageContainer.helpCommand() -> Result.success(HelpCommand)
        languageContainer.startCommand() -> Result.success(StartCommand)
        else -> Result.failure(Exception("Command mismatch: $command"))
    }

fun slashCommandRouter(context: InteractionContext<SlashCommandInteractionEvent>): Mono<Tuple2<InteractionContext<SlashCommandInteractionEvent>, Result<CommandReport>>> =
    Mono.zip(context.toMono(),
            matchCommand(
                command = context.event.name,
                languageContainer = context.languageContainer
            ).toMono()
        )
        .filter { it.t2.isSuccess }
        .doOnNext { it.t1.event
            .deferReply().queue()
        }
        .flatMap { Mono.zip(it.t1.toMono(), it.t2.getOrThrow()
            .parse(event = it.t1.event, languageContainer = it.t1.languageContainer).toMono()
        ) }
        .filter { it.t2.isSuccess }
        .flatMap { Mono.zip(it.t1.toMono(), mono { it.t2.getOrThrow()
            .execute(
                botContext = it.t1.botContext,
                languageContainer = it.t1.languageContainer,
                user = UserId(it.t1.event.user.idLong),
            ) { msg -> WebHookRestActionAdaptor(it.t1.event.hook.sendMessage(msg)) }
        }.toMono()) }

const val COMMAND_PREFIX = 126.toChar() // "~"

const val EMOJI_CHECK = "\u2611\uFE0F" // ☑
const val EMOJI_CROSS = "\u274C" // ❌

fun textCommandRouter(context: InteractionContext<MessageReceivedEvent>): Mono<Tuple2<InteractionContext<MessageReceivedEvent>, Result<CommandReport>>> =
        Mono.zip(context.toMono(),
            matchCommand(
                command = context.event.message.contentRaw.split(" ")[0].substring(1),
                languageContainer = context.languageContainer
            ).toMono()
        )
        .doOnNext {
            if (it.t2.isFailure) it.t1.event.message.addReaction(EMOJI_CROSS).queue()
        }
        .filter { it.t2.isSuccess }
        .flatMap { Mono.zip(it.t1.toMono(), it.t2.getOrThrow()
            .parse(event = it.t1.event, languageContainer = it.t1.languageContainer).toMono()
        ) }
        .doOnNext {
            if (it.t2.isSuccess) it.t1.event.message.addReaction(EMOJI_CHECK).queue()
            else it.t1.event.message.addReaction(EMOJI_CROSS).queue()
        }
        .filter { it.t2.isSuccess }
        .flatMap { Mono.zip(it.t1.toMono(), mono { it.t2.getOrThrow()
            .execute(
                botContext = it.t1.botContext,
                languageContainer = it.t1.languageContainer,
                user = UserId(it.t1.event.author.idLong),
            ) { msg -> MessageActionRestActionAdaptor(it.t1.event.message.reply(msg)) }
        }) }
        .map { Tuples.of(it.t1, it.t2) }
