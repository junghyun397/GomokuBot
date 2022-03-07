import club.minnced.jda.reactor.ReactiveEventManager
import club.minnced.jda.reactor.on
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.ShutdownEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import reactor.kotlin.core.publisher.toMono
import route.matchButtonInteraction
import route.matchCommand
import session.SessionManager
import utility.GuildId
import utility.getLogger

data class Token(val token: String)

fun getTokenFromEnv(): Token = Token(
    token = System.getenv("GOMOKUBOT_DISCORD_TOKEN")
)

data class B3nzeneConfig(val serverAddress: String, val serverPort: Int)

fun getB3nzeneConfigFromEnv(): B3nzeneConfig = B3nzeneConfig(
    serverAddress = System.getenv("GOMOKUBOT_B3NZENE_ADDRESS"),
    serverPort = System.getenv("GOMOKUBOT_B3NZENE_PORT").toInt()
)

data class MySQLConfig(val serverURL: String, val serverUname: String, val serverPassword: String)

fun getMySQLConfigFromEnv(): MySQLConfig = MySQLConfig(
    serverURL = System.getenv("GOMOKUBOT_DB_URL"),
    serverUname = System.getenv("GOMOKUBOT_DB_UNAME"),
    serverPassword = System.getenv("GOMOKUBOT_DB_PASSWORD")
)

const val commandPrefix: Char = 126.toChar() // "~"

fun buildJDAManager(): ReactiveEventManager {
    val logger = getLogger<ReactiveEventManager>()

    val eventManager = ReactiveEventManager()

    eventManager.on<SlashCommandInteractionEvent>()
        .filter { !it.user.isBot }
        .flatMap { event -> event.toMono().zipWith( mono {
            SessionManager.getLanguageContainer(GuildId(event.guild!!.idLong))
        }) }
        .flatMap { combined -> (combined.t1 to
            matchCommand(
                command = combined.t1.commandString,
                languageContainer = combined.t2
            )).toMono()
        }
        .filter { it.second.isSuccess }
        .doOnNext { it.first
            .deferReply()
        }
        .flatMap { combined -> (combined.first to combined.second.getOrThrow()
            .parse(combined.first)
        ).toMono() }
        .filter { it.second.isSuccess }
        .flatMap { combined -> mono {
            combined.second.getOrThrow()
                .execute(combined.first.user) { msg ->
                    combined.first.hook.sendMessage(msg).queue()
                }
        } }
        .subscribe {
            TODO("log")
        }

    eventManager.on<MessageReceivedEvent>()
        .filter { !it.author.isBot && it.message.contentRaw.startsWith(commandPrefix) }
        .flatMap { event -> event.toMono().zipWith( mono {
            SessionManager.getLanguageContainer(GuildId(event.guild.idLong))
        }) }
        .flatMap { combined -> (combined.t1 to
            matchCommand(
                command = combined.t1.message.contentRaw.split(" ")[0].substring(1),
                languageContainer = combined.t2
            )
        ).toMono() }
        .filter { it.second.isSuccess }
        .flatMap { combined -> (combined.first to combined.second.getOrThrow()
            .parse(combined.first)
        ).toMono() }
        .flatMap { combined -> mono {
            combined.first to combined.second.getOrThrow()
                .execute(combined.first.author) { msg ->
                    combined.first.message.reply(msg).queue()
                }
        } }
        .doOnNext {
            if (it.second.isSuccess) it.first.message.addReaction("\u2611\uFE0F").queue()
            else it.first.message.addReaction("\u274C").queue()
        }
        .flatMap { combined -> (combined.second).toMono() }
        .subscribe {
            TODO("log")
        }

    eventManager.on<ButtonInteractionEvent>()
        .filter { !it.user.isBot }
        .map { matchButtonInteraction(it) }
        .subscribe {
            TODO("log")
        }

    eventManager.on<GuildJoinEvent>()
        .subscribe {
            TODO("log")
        }

    eventManager.on<ShutdownEvent>()
        .subscribe {
            TODO("log")
        }

    return eventManager
}

fun main() {
    JDABuilder
        .createLight(getTokenFromEnv().token)
        .setEventManager(buildJDAManager())
        .setActivity(Activity.playing("/help or ${commandPrefix}help"))
        .setStatus(OnlineStatus.ONLINE)
        .build()
}
