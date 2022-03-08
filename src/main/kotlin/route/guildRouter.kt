package route

import BotContext
import interact.message.MessageAgent
import interact.reports.GuildJoinReport
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import session.SessionManager
import utility.GuildId
import utility.MessagePublisher

fun buildGuildJoinHandler(botContext: BotContext): (GuildJoinEvent) -> Mono<GuildJoinReport> =
    { event ->
        Mono.zip(event.toMono(), botContext.toMono())
            .doOnNext { combined -> runBlocking { launch {
                SessionManager.retrieveLanguageContainer(combined.t2.sessionRepository, GuildId(combined.t1.guild.idLong)).let {
                    val messagePublisher: MessagePublisher =
                        { msg: Message -> combined.t1.guild.defaultChannel?.sendMessage(msg) }

                    MessageAgent.sendHelpAbout(messagePublisher)
                    MessageAgent.sendHelpCommand(messagePublisher)
                    MessageAgent.sendHelpSkin(messagePublisher)
                    MessageAgent.sendHelpLanguage(messagePublisher)
                    }
                } }
            }
            .flatMap { GuildJoinReport(it.t1.guild.idLong, it.t1.guild.name).toMono() }
    }
