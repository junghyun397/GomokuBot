package route

import interact.commands.buildableCommands
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
        val commandInserted = run {
            buildableCommands.forEach { command ->
                context.event.guild.upsertCommand(command.buildCommandData(context.guildConfig.language.container)).queue()
            }
            true
        }

        val helpSent = run {
            context.event.guild.defaultChannel?.let { channel ->
                val messagePublisher: MessagePublisher =
                    { msg -> MessageActionRestActionAdaptor(channel.sendMessage(msg)) }

                MessageAgent.sendHelpAbout(messagePublisher, context.guildConfig.language.container)
                MessageAgent.sendHelpCommand(messagePublisher, context.guildConfig.language.container)
                MessageAgent.sendHelpStyle(messagePublisher, context.guildConfig.language.container)
                MessageAgent.sendHelpLanguage(messagePublisher, context.guildConfig.language.container)
                true
            }
            false
        }

        GuildJoinReport(commandInserted = commandInserted, helpSent = helpSent)
    }.toMono())
