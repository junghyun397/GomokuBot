import club.minnced.jda.reactor.ReactiveEventManager
import club.minnced.jda.reactor.on
import database.DatabaseConnection
import inference.InferenceRepository
import interact.reports.InteractionReport
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.ShutdownEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import reactor.util.function.Tuple2
import route.*
import session.SessionRepository
import utility.getLogger

data class Token(val token: String) {
    companion object {
        fun fromEnv() = Token(token = System.getenv("GOMOKUBOT_DISCORD_TOKEN"))
    }
}

data class B3nzeneConfig(val serverAddress: String, val serverPort: Int) {
    companion object {
        fun fromEnv() = B3nzeneConfig(
            serverAddress = System.getenv("GOMOKUBOT_B3NZENE_ADDRESS"),
            serverPort = System.getenv("GOMOKUBOT_B3NZENE_PORT").toInt()
        )
    }
}

data class MySQLConfig(val serverURL: String, val serverUname: String, val serverPassword: String) {
    companion object {
        fun fromEnv() = MySQLConfig(
            serverURL = System.getenv("GOMOKUBOT_DB_URL"),
            serverUname = System.getenv("GOMOKUBOT_DB_UNAME"),
            serverPassword = System.getenv("GOMOKUBOT_DB_PASSWORD")
        )
    }
}

const val COMMAND_PREFIX: Char = 126.toChar() // "~"

inline fun <reified E : Event, reified R : InteractionReport> processLog(combined: Tuple2<InteractionContext<E>, Result<R>>) =
    getLogger<R>().let { logger ->
        combined.t2.onSuccess {
            logger.info("${combined.t1.guildName}/${combined.t1.guildId}" +
                    "T${(it.terminationTime.timestamp - combined.t1.emittenTime.timestamp)/1000}ms => $it")
        }
        combined.t2.onFailure { logger.error(it.message) }
    }

fun main() {
    val databaseConnection = DatabaseConnection()
    val sessionRepository = SessionRepository(databaseConnection = databaseConnection)
    val inferenceRepository = InferenceRepository()

    val botContext = BotContext(databaseConnection, sessionRepository, inferenceRepository)

    val logger = getLogger<ReactiveEventManager>()

    val eventManager = ReactiveEventManager()

    eventManager.on<SlashCommandInteractionEvent>()
        .filter { it.isFromGuild && !it.user.isBot }
        .map { InteractionContext.of(botContext, it, it.guild!!) }
        .flatMap(buildSlashCommandHandler())
        .doOnNext { processLog(it) }
        .subscribe()

    eventManager.on<MessageReceivedEvent>()
        .filter { it.isFromGuild && !it.author.isBot && it.message.contentRaw.startsWith(COMMAND_PREFIX) }
        .map { InteractionContext.of(botContext, it, it.guild) }
        .flatMap(buildTextCommandHandler())
        .doOnNext { processLog(it) }
        .subscribe()

    eventManager.on<ButtonInteractionEvent>()
        .filter { it.isFromGuild && !it.user.isBot }
        .map { InteractionContext.of(botContext, it, it.guild!!) }
        .flatMap(buildButtonInteractionHandler())
        .doOnNext { processLog(it) }
        .subscribe()

    eventManager.on<GuildJoinEvent>()
        .map { InteractionContext.of(botContext, it, it.guild) }
        .flatMap(buildGuildJoinHandler())
        .doOnNext { processLog(it) }
        .subscribe()

    eventManager.on<ShutdownEvent>()
        .subscribe()

    JDABuilder
        .createLight(Token.fromEnv().token)
        .setEventManager(eventManager)
        .setActivity(Activity.playing("/help or ${COMMAND_PREFIX}help"))
        .setStatus(OnlineStatus.ONLINE)
        .build()
}
