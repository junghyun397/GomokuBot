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
import session.SessionManager
import utility.GuildId
import utility.MessageActionRestActionAdaptor
import utility.UserId
import utility.WebHookRestActionAdaptor

private fun matchCommand(command: String, languageContainer: LanguageContainer): Result<ParsableCommand> =
    when (command) {
        languageContainer.helpCommand() -> Result.success(HelpCommand)
        languageContainer.startCommand() -> Result.success(StartCommand)
        else -> Result.failure(Exception("Command mismatch: $command"))
    }

fun buildSlashCommandHandler(): (InteractionContext<SlashCommandInteractionEvent>) -> Mono<Tuple2<InteractionContext<SlashCommandInteractionEvent>, Result<CommandReport>>> =
    { context ->
        Mono.zip(context.toMono(), mono {
            SessionManager.retrieveLanguageContainer(context.botContext.sessionRepository, GuildId(context.event.guild!!.idLong))
        })
            .flatMap { Mono.zip(it.t1.toMono(), it.t2.toMono(),
                matchCommand(
                    command = it.t1.event.commandString,
                    languageContainer = it.t2
                ).toMono()
            ) }
            .filter { it.t3.isSuccess }
            .doOnNext { it.t1.event
                .deferReply()
            }
            .flatMap { Mono.zip(it.t1.toMono(), it.t2.toMono(), it.t3.getOrThrow()
                .parse(event = it.t1.event, languageContainer = it.t2).toMono()
            ) }
            .filter { it.t3.isSuccess }
            .flatMap { Mono.zip(it.t1.toMono(), mono { it.t3.getOrThrow()
                .execute(
                    botContext = it.t1.botContext,
                    languageContainer = it.t2,
                    user = UserId(it.t1.event.user.idLong),
                    guild = GuildId(it.t1.event.guild!!.idLong)
                ) { msg -> WebHookRestActionAdaptor(it.t1.event.hook.sendMessage(msg)) }
            }.toMono()) }
    }

const val EMOJI_CHECK = "\u2611\uFE0F" // ☑
const val EMOJI_CROSS = "\u274C" //

fun buildTextCommandHandler(): (InteractionContext<MessageReceivedEvent>) -> Mono<Tuple2<InteractionContext<MessageReceivedEvent>, Result<CommandReport>>> =
    { context ->
        Mono.zip(context.toMono() , mono {
            SessionManager.retrieveLanguageContainer(context.botContext.sessionRepository, GuildId(context.event.guild.idLong))
        })
            .flatMap { Mono.zip(it.t1.toMono(), it.t2.toMono(),
                matchCommand(
                    command = it.t1.event.message.contentRaw.split(" ")[0].substring(1),
                    languageContainer = it.t2
                ).toMono()
            ) }
            .doOnNext {
                if (it.t3.isFailure) it.t1.event.message.addReaction(EMOJI_CROSS).queue()
            }
            .filter { it.t3.isSuccess }
            .flatMap { Mono.zip(it.t1.toMono(), it.t2.toMono(), it.t3.getOrThrow()
                .parse(event = it.t1.event, languageContainer = it.t2).toMono()
            ) }
            .doOnNext {
                if (it.t3.isSuccess) it.t1.event.message.addReaction(EMOJI_CHECK).queue()
                else it.t1.event.message.addReaction(EMOJI_CROSS).queue()
            }
            .filter { it.t3.isSuccess }
            .flatMap { Mono.zip(it.t1.toMono(), it.t2.toMono(), mono { it.t3.getOrThrow()
                .execute(
                    botContext = it.t1.botContext,
                    languageContainer = it.t2,
                    user = UserId(it.t1.event.author.idLong),
                    guild = GuildId(it.t1.event.guild.idLong)
                ) { msg -> MessageActionRestActionAdaptor(it.t1.event.message.reply(msg)) }
            }) }
            .map { Tuples.of(it.t1, it.t3) }
    }
