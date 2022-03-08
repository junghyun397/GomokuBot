package route

import BotContext
import interact.reports.CommandReport
import interact.commands.HelpCommand
import interact.commands.ParsableCommand
import interact.commands.StartCommand
import interact.i18n.LanguageContainer
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import session.SessionManager
import utility.GuildId

private fun matchCommand(command: String, languageContainer: LanguageContainer): Result<ParsableCommand> =
    when (command) {
        languageContainer.helpCommand() -> Result.success(HelpCommand)
        languageContainer.rankCommand() -> Result.success(StartCommand)
        else -> Result.failure(Exception("Command mismatch: $command"))
    }

fun buildSlashCommandHandler(botContext: BotContext): (SlashCommandInteractionEvent) -> Mono<Result<CommandReport>> =
    { event ->
        Mono.zip(event.toMono(), botContext.toMono())
            .flatMap { Mono.zip(it.t1.toMono(), it.t2.toMono(), mono {
                SessionManager.retrieveLanguageContainer(it.t2.sessionRepository, GuildId(it.t1.guild!!.idLong))
            }) }
            .flatMap { Mono.zip(it.t1.toMono(), it.t2.toMono(),
                matchCommand(
                    command = it.t1.commandString,
                    languageContainer = it.t3
                ).toMono()
            ) }
            .filter { it.t3.isSuccess }
            .doOnNext { it.t1
                .deferReply()
            }
            .flatMap { Mono.zip(it.t1.toMono(), it.t2.toMono(), it.t3.getOrThrow()
                .parse(event = it.t1).toMono()
            ) }
            .filter { it.t3.isSuccess }
            .flatMap { mono { it.t3.getOrThrow()
                .execute(botContext = it.t2, user = it.t1.user) { msg ->
                    it.t1.hook.sendMessage(msg).queue()
                }
            } }
    }

const val EMOJI_CHECK = "\u2611\uFE0F" // ☑
const val EMOJI_CROSS = "\u274C" //

fun buildTextCommandHandler(botContext: BotContext): (MessageReceivedEvent) -> Mono<Result<CommandReport>> =
    { event ->
        Mono.zip(event.toMono(), botContext.toMono())
            .flatMap { Mono.zip(it.t1.toMono(), it.t2.toMono(), mono {
                SessionManager.retrieveLanguageContainer(it.t2.sessionRepository, GuildId(it.t1.guild.idLong))
            }) }
            .flatMap { Mono.zip(it.t1.toMono(), it.t2.toMono(),
                matchCommand(
                    command = it.t1.message.contentRaw.split(" ")[0].substring(1),
                    languageContainer = it.t3
                ).toMono()
            ) }
            .filter { it.t3.isSuccess }
            .flatMap { Mono.zip(it.t1.toMono(), it.t2.toMono(), it.t3.getOrThrow()
                .parse(event = it.t1).toMono()
            ) }
            .doOnNext {
                if (it.t3.isFailure) it.t1.message.addReaction(EMOJI_CROSS).queue()
            }
            .filter { it.t3.isSuccess }
            .flatMap { Mono.zip(it.t1.toMono(), it.t2.toMono(), mono { it.t3.getOrThrow()
                .execute(botContext = it.t2, user = it.t1.author) { msg ->
                    it.t1.message.reply(msg).queue()
                }
            }) }
            .doOnNext {
                if (it.t3.isSuccess) it.t1.message.addReaction(EMOJI_CHECK).queue()
                else it.t1.message.addReaction(EMOJI_CROSS).queue()
            }
            .flatMap { it.t3.toMono() }
    }
