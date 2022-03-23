package route

import interact.GuildManager
import interact.message.MessageAgent
import interact.reports.GuildJoinReport
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple2
import utility.MessageActionRestActionAdaptor
import utility.MessagePublisher

fun guildJoinRouter(context: InteractionContext<GuildJoinEvent>): Mono<Tuple2<InteractionContext<GuildJoinEvent>, Result<GuildJoinReport>>> =
    Mono.zip(context.toMono(), runCatching {
        val commandInserted = run {
            context.event.guild.selfMember.hasPermission(Permission.MESSAGE_SEND)
            GuildManager.insertCommands(context.event.guild, context.guildConfig.language.container)
            true
        }

        val helpSent = context.event.guild.defaultChannel?.let { channel ->
            val messagePublisher: MessagePublisher = { msg -> GuildManager.permissionSafeRun(channel, Permission.MESSAGE_SEND) {
                MessageActionRestActionAdaptor(channel.sendMessage(msg))
            } }

            MessageAgent.sendEmbedAbout(messagePublisher, context.guildConfig.language.container)
                .flatMap { MessageAgent.sendEmbedCommand(messagePublisher, context.guildConfig.language.container) }
                .flatMap { MessageAgent.sendEmbedStyle(messagePublisher, context.guildConfig.language.container) }
                .flatMap { MessageAgent.sendEmbedLanguage(messagePublisher) }
                .fold(
                    onDefined = { true },
                    onEmpty = { false }
                )
        }

        GuildJoinReport(commandInserted, helpSent)
    }.toMono())
