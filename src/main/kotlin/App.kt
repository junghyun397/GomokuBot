import club.minnced.jda.reactor.ReactiveEventManager
import club.minnced.jda.reactor.on
import database.DatabaseConnection
import inference.InferenceRepository
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.ShutdownEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import route.buildButtonInteractionHandler
import route.buildGuildJoinHandler
import route.buildSlashCommandHandler
import route.buildTextCommandHandler
import session.SessionRepository
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

const val COMMAND_PREFIX: Char = 126.toChar() // "~"

fun main() {
    val databaseConnection = DatabaseConnection()
    val sessionRepository = SessionRepository(databaseConnection = databaseConnection)
    val inferenceRepository = InferenceRepository()

    val botContext = BotContext(databaseConnection, sessionRepository, inferenceRepository)

    val logger = getLogger<ReactiveEventManager>()

    val eventManager = ReactiveEventManager()

    eventManager.on<SlashCommandInteractionEvent>()
        .filter { !it.user.isBot }
        .flatMap(buildSlashCommandHandler(botContext))
        .subscribe()

    eventManager.on<MessageReceivedEvent>()
        .filter { !it.author.isBot && it.message.contentRaw.startsWith(COMMAND_PREFIX) }
        .flatMap(buildTextCommandHandler(botContext))
        .subscribe()

    eventManager.on<ButtonInteractionEvent>()
        .filter { !it.user.isBot }
        .flatMap(buildButtonInteractionHandler((botContext)))
        .subscribe()

    eventManager.on<GuildJoinEvent>()
        .flatMap(buildGuildJoinHandler(botContext))
        .subscribe()

    eventManager.on<ShutdownEvent>()
        .subscribe()

    JDABuilder
        .createLight(getTokenFromEnv().token)
        .setEventManager(eventManager)
        .setActivity(Activity.playing("/help or ${COMMAND_PREFIX}help"))
        .setStatus(OnlineStatus.ONLINE)
        .build()
}
