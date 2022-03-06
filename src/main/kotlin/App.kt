import club.minnced.jda.reactor.ReactiveEventManager
import club.minnced.jda.reactor.on
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.ShutdownEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import route.mapButtonInteraction
import route.mapCommand

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

const val commandPrefix = "~"

fun main() {
    JDABuilder.createLight(getTokenFromEnv().token)
        .setEventManager(buildJDAManager())
        .setActivity(Activity.playing("/help or ${commandPrefix}help"))
        .setStatus(OnlineStatus.ONLINE)
        .build()
}

fun buildJDAManager(): ReactiveEventManager {
    val manager = ReactiveEventManager()

    manager.on<SlashCommandInteractionEvent>()
        .filter { !it.user.isBot }
        .map { Pair(it, mapCommand(it.commandString, TODO())) }
        .filter { it.second.isSuccess }
        .doOnEach { it.get()!!.first.deferReply() }
        .map { it.second.getOrThrow().process(it.first.user, it.first.channel) }
        .subscribe()

    manager.on<MessageReceivedEvent>()
        .filter { !it.author.isBot and it.message.contentRaw.startsWith(commandPrefix) }
        .map { Pair(it, mapCommand(it.message.contentRaw.split(" ")[0].substring(1), TODO())) }
        .filter { it.second.isSuccess }
        .map { Pair(it.first, it.second.getOrThrow().process(it.first.author, it.first.channel)) }
        .doOnEach {
            if (it.get()!!.second.isSuccess)
                it.get()!!.first.message
                    .addReaction("\u2611\uFE0F")
                    .queue()
            else
                it.get()!!.first.message
                    .addReaction("\u274C")
                    .queue()
        }
        .subscribe()

    manager.on<ButtonInteractionEvent>()
        .filter { !it.user.isBot }
        .map { mapButtonInteraction(it) }
        .subscribe()

    manager.on<GuildJoinEvent>()
        .subscribe()

    manager.on<ShutdownEvent>()
        .subscribe()

    return manager
}
