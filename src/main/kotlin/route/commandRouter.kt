package route

import BotContext
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

fun buildSlashCommandHandler(botContext: BotContext): (SlashCommandInteractionEvent) -> Mono<Result<CommandReport>> =
    { event ->
        (botContext to event).toMono()
            .flatMap { Mono.zip(it.toMono(), mono {
                SessionManager.retrieveLanguageContainer(it.first.sessionRepository, GuildId(it.second.guild!!.idLong))
            }) }
            .flatMap { Mono.zip(it.t1.toMono(), it.t2.toMono(),
                matchCommand(
                    command = it.t1.second.commandString,
                    languageContainer = it.t2
                ).toMono()
            ) }
            .filter { it.t3.isSuccess }
            .doOnNext { it.t1.second
                .deferReply()
            }
            .flatMap { Mono.zip(it.t1.toMono(), it.t2.toMono(), it.t3.getOrThrow()
                .parse(event = it.t1.second, languageContainer = it.t2).toMono()
            ) }
            .filter { it.t3.isSuccess }
            .flatMap { mono { it.t3.getOrThrow()
                .execute(
                    botContext = it.t1.first,
                    languageContainer = it.t2,
                    user = UserId(it.t1.second.user.idLong),
                    guild = GuildId(it.t1.second.guild!!.idLong)
                ) { msg -> WebHookRestActionAdaptor(it.t1.second.hook.sendMessage(msg)) }
            } }
    }

const val EMOJI_CHECK = "\u2611\uFE0F" // ☑
const val EMOJI_CROSS = "\u274C" //

fun buildTextCommandHandler(botContext: BotContext): (MessageReceivedEvent) -> Mono<Result<CommandReport>> =
    { event ->
        (botContext to event).toMono()
            .flatMap { Mono.zip(it.toMono(), mono {
                SessionManager.retrieveLanguageContainer(it.first.sessionRepository, GuildId(it.second.guild.idLong))
            }) }
            .flatMap { Mono.zip(it.t1.toMono(), it.t2.toMono(),
                matchCommand(
                    command = it.t1.second.message.contentRaw.split(" ")[0].substring(1),
                    languageContainer = it.t2
                ).toMono()
            ) }
            .doOnNext {
                if (it.t3.isFailure) it.t1.second.message.addReaction(EMOJI_CROSS).queue()
            }
            .filter { it.t3.isSuccess }
            .flatMap { Mono.zip(it.t1.toMono(), it.t2.toMono(), it.t3.getOrThrow()
                .parse(event = it.t1.second, languageContainer = it.t2).toMono()
            ) }
            .doOnNext {
                if (it.t3.isSuccess) it.t1.second.message.addReaction(EMOJI_CHECK).queue()
                else it.t1.second.message.addReaction(EMOJI_CROSS).queue()
            }
            .filter { it.t3.isSuccess }
            .flatMap { Mono.zip(it.t1.toMono(), it.t2.toMono(), mono { it.t3.getOrThrow()
                .execute(
                    botContext = it.t1.first,
                    languageContainer = it.t2,
                    user = UserId(it.t1.second.author.idLong),
                    guild = GuildId(it.t1.second.guild.idLong)
                ) { msg -> MessageActionRestActionAdaptor(it.t1.second.message.reply(msg)) }
            }) }
            .flatMap { it.t3.toMono() }
    }
