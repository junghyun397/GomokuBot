package route

import interact.commands.BuildableCommand
import interact.commands.entities.HelpCommand
import interact.commands.entities.StartCommand
import interact.message.MessageAgent
import interact.reports.GuildJoinReport
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple2
import session.SessionManager
import utility.GuildId
import utility.MessageActionRestActionAdaptor
import utility.MessagePublisher

private val commands: Collection<BuildableCommand> =
    listOf(HelpCommand, StartCommand)

val guildJoinHandler: (InteractionContext<GuildJoinEvent>) -> Mono<Tuple2<InteractionContext<GuildJoinEvent>, Result<GuildJoinReport>>> =
    { context ->
        Mono.zip(context.toMono(), mono {
            SessionManager.retrieveLanguageContainer(context.botContext.sessionRepository, GuildId(context.event.guild.idLong)).let {
                // return@mono Result.success(GuildJoinReport(commandInserted = false)) // TODO()
                commands.forEach { command -> context.event.guild.upsertCommand(command.buildCommandData(it)) }

                context.event.guild.defaultChannel?.let { channel ->
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
        })
    }
