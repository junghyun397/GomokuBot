package route

import BotContext
import interact.commands.BuildableCommand
import interact.commands.entities.HelpCommand
import interact.commands.entities.StartCommand
import interact.message.MessageAgent
import interact.reports.GuildJoinReport
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import session.SessionManager
import utility.GuildId
import utility.MessageActionRestActionAdaptor
import utility.MessagePublisher

private val commands: Collection<BuildableCommand> =
    listOf(HelpCommand, StartCommand)

fun buildGuildJoinHandler(botContext: BotContext): (GuildJoinEvent) -> Mono<Result<GuildJoinReport>> =
    { event ->
        Mono.zip(event.toMono(), botContext.toMono())
            .flatMap { combined -> mono {
                SessionManager.retrieveLanguageContainer(combined.t2.sessionRepository, GuildId(combined.t1.guild.idLong)).let {
                    commands.forEach { command -> combined.t1.guild.upsertCommand(command.buildCommandData(it)) }

                    combined.t1.guild.defaultChannel?.let { channel ->
                        val messagePublisher: MessagePublisher =
                            { msg -> MessageActionRestActionAdaptor(channel.sendMessage(msg)) }

                        MessageAgent.sendHelpAbout(messagePublisher, it)
                        MessageAgent.sendHelpCommand(messagePublisher, it)
                        MessageAgent.sendHelpSkin(messagePublisher, it)
                        MessageAgent.sendHelpLanguage(messagePublisher, it)

                        return@mono Result.success(GuildJoinReport())
                    }

                    return@mono Result.success(GuildJoinReport(helpSent = false))
                }
            } }
    }
