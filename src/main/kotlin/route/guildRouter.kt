package route

import interact.commands.buildableCommand
import interact.message.MessageAgent
import interact.reports.GuildJoinReport
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple2
import utility.MessageActionRestActionAdaptor
import utility.MessagePublisher

fun guildJoinRouter(context: InteractionContext<GuildJoinEvent>): Mono<Tuple2<InteractionContext<GuildJoinEvent>, Result<GuildJoinReport>>> =
    Mono.zip(context.toMono(), runCatching {
        // GuildJoinReport(commandInserted = false) TODO()
        buildableCommand.forEach { command ->
            context.event.guild.upsertCommand(command.buildCommandData(context.languageContainer)).queue()
        }

        context.event.guild.defaultChannel?.let { channel ->
            val messagePublisher: MessagePublisher =
                { msg -> MessageActionRestActionAdaptor(channel.sendMessage(msg)) }

            MessageAgent.sendHelpAbout(messagePublisher, context.languageContainer)
            MessageAgent.sendHelpCommand(messagePublisher, context.languageContainer)
            MessageAgent.sendHelpSkin(messagePublisher, context.languageContainer)

            MessageAgent.sendHelpLanguage(messagePublisher, context.languageContainer)
            GuildJoinReport()
        }

        GuildJoinReport(helpSent = false)
    }.toMono())
