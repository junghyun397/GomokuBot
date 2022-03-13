import club.minnced.jda.reactor.ReactiveEventManager
import club.minnced.jda.reactor.on
import database.DatabaseConnection
import inference.B3nzeneConnection
import interact.reports.InteractionReport
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.ShutdownEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import reactor.util.function.Tuple2
import route.*
import session.SessionRepository
import utility.asciiLogo
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

inline fun <reified E : Event, reified R : InteractionReport> leaveLog(combined: Tuple2<InteractionContext<E>, Result<R>>) =
    getLogger<R>().let { logger ->
        combined.t2.onSuccess {
            logger.info("${E::class.simpleName} ${combined.t1.guildName}/${combined.t1.guildId.id} " +
                    "T${(it.terminationTime.timestamp - combined.t1.emittenTime.timestamp)/1000}ms => $it")
        }
        combined.t2.onFailure {
            logger.error("${E::class.simpleName} ${combined.t1.guildName}/${combined.t1.guildId.id} " +
                    "T${(System.currentTimeMillis() - combined.t1.emittenTime.timestamp)/1000}ms => ${it.stackTraceToString()}")
        }
    }

object App {

    fun launch() {
        val logger = getLogger<App>()

        val mySQLConfig = MySQLConfig.fromEnv()
        val b3nzeneConfig = B3nzeneConfig.fromEnv()

        val databaseConnection = DatabaseConnection
            .connectionFrom(mySQLConfig.serverURL, mySQLConfig.serverUname, mySQLConfig.serverPassword)
        logger.info("mysql database connected.")

        val b3nzeneConnection = B3nzeneConnection
            .connectionFrom(b3nzeneConfig.serverAddress, b3nzeneConfig.serverPort)
        logger.info("b3nzene inference service connected.")

        val sessionRepository = SessionRepository(databaseConnection = databaseConnection)

        val botContext = BotContext(databaseConnection, b3nzeneConnection, sessionRepository)

        val eventManager = ReactiveEventManager()

        eventManager.on<SlashCommandInteractionEvent>()
            .filter { it.isFromGuild && !it.user.isBot }
            .map { InteractionContext.of(botContext, it, it.guild!!) }
            .flatMap(slashCommandHandler)
            .doOnNext { leaveLog(it) }
            .subscribe()

        eventManager.on<MessageReceivedEvent>()
            .filter { it.isFromGuild && !it.author.isBot && it.message.contentRaw.startsWith(COMMAND_PREFIX) }
            .map { InteractionContext.of(botContext, it, it.guild) }
            .flatMap(textCommandHandler)
            .doOnNext { leaveLog(it) }
            .subscribe()

        eventManager.on<ButtonInteractionEvent>()
            .filter { it.isFromGuild && !it.user.isBot }
            .map { InteractionContext.of(botContext, it, it.guild!!) }
            .flatMap(buttonInteractionHandler)
            .doOnNext { leaveLog(it) }
            .subscribe()

        eventManager.on<GuildJoinEvent>()
            .map { InteractionContext.of(botContext, it, it.guild) }
            .flatMap(guildJoinHandler)
            .doOnNext { leaveLog(it) }
            .subscribe()

        eventManager.on<ReadyEvent>()
            .doOnNext { logger.info("jda ready, complete loading.") }
            .subscribe()

        eventManager.on<ShutdownEvent>()
            .doOnNext { logger.info("jda shutdown.") }
            .subscribe()

        logger.info("reactive event manager ready.")

        JDABuilder
            .createLight(Token.fromEnv().token)
            .setEventManager(eventManager)
            .setActivity(Activity.playing("/help or ${COMMAND_PREFIX}help"))
            .setStatus(OnlineStatus.ONLINE)
            .build()
    }
}

fun main() {
    val logger = getLogger<App>()
    logger.info(asciiLogo)

    val launchResult = runCatching { App.launch() }

    launchResult.onSuccess { logger.info("gomokubot ready.") }
    launchResult.onFailure { logger.error(it.stackTraceToString()) }
}
