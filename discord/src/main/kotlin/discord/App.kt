package discord

import club.minnced.jda.reactor.ReactiveEventManager
import club.minnced.jda.reactor.on
import core.BotConfig
import core.BotContext
import core.assets.Guild
import core.assets.GuildUid
import core.database.DatabaseManager
import core.database.LocalCaches
import core.database.repositories.GuildProfileRepository
import core.database.repositories.UserProfileRepository
import core.inference.KvineClient
import core.interact.reports.InteractionReport
import core.session.SessionManager
import core.session.SessionRepository
import discord.assets.*
import discord.interact.InteractionContext
import discord.route.*
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.ShutdownEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import reactor.util.function.Tuple2
import utils.assets.LinuxTime
import utils.log.getLogger
import utils.structs.forEach
import java.util.*
import kotlin.reflect.KClass

private data class Token(val token: String) {
    companion object {
        fun fromEnv() = Token(token = System.getenv("GOMOKUBOT_DISCORD_TOKEN"))
    }
}

private data class PostgreSQLConfig(val serverURL: String) {
    companion object {
        fun fromEnv() = PostgreSQLConfig(
            serverURL = System.getenv("GOMOKUBOT_DB_URL")
        )
    }
}

private data class KvineConfig(val serverAddress: String, val serverPort: Int) {
    companion object {
        fun fromEnv() = KvineConfig(
            serverAddress = System.getenv("GOMOKUBOT_KVINE_ADDRESS"),
            serverPort = System.getenv("GOMOKUBOT_KVINE_PORT").toInt()
        )
    }
}

private inline fun <reified E : Event, R : InteractionReport> leaveLog(combined: Tuple2<InteractionContext<E>, Result<R>>) =
    combined.t2
        .onSuccess{
            logger.info("${E::class.simpleName} ${combined.t1.guild} " +
                    "T${(it.terminationTime.timestamp - combined.t1.emittedTime.timestamp)}ms => $it")
        }
        .onFailure {
            logger.error("${E::class.simpleName} ${combined.t1.guild} " +
                    "T${(System.currentTimeMillis() - combined.t1.emittedTime.timestamp)}ms => ${it.stackTraceToString()}")
        }

private fun leaveLog(event: KClass<*>, error: Throwable) =
    logger.error("${event.simpleName} ${error.stackTraceToString()}")

private suspend inline fun <E : Event> retrieveInteractionContext(bot: BotContext, event: E, jdaUser: net.dv8tion.jda.api.entities.User, jdaGuild: net.dv8tion.jda.api.entities.Guild): InteractionContext<E> {
    val user = UserProfileRepository.retrieveOrInsertUser(bot.dbConnection, DISCORD_PLATFORM_ID, jdaUser.extractId()) {
        jdaUser.buildNewProfile()
    }

    val guild = GuildProfileRepository.retrieveOrInsertGuild(bot.dbConnection, DISCORD_PLATFORM_ID, jdaGuild.extractId()) {
        Guild(
            id = GuildUid(UUID.randomUUID()),
            platform = DISCORD_PLATFORM_ID,
            givenId = jdaGuild.extractId(),
            name = jdaGuild.name,
        )
    }

    return InteractionContext(
        bot = bot,
        event = event,
        user = user,
        guild = guild,
        config = SessionManager.retrieveGuildConfig(bot.sessions, guild),
        emittedTime = LinuxTime()
    )
}

object GomokuBot {

    fun launch() {
        val botConfig = BotConfig()

        val postgresqlConfig = PostgreSQLConfig.fromEnv()
        val kvineConfig = KvineConfig.fromEnv()

        val dbConnection = runBlocking {
            DatabaseManager.newConnectionFrom(postgresqlConfig.serverURL, LocalCaches())
                .also { connection ->
                    DatabaseManager.initDatabase(connection)
                    DatabaseManager.initCaches(connection)
                }
        }

        logger.info("postgresql database connected.")

        val kvineClient = KvineClient
            .connectionFrom(kvineConfig.serverAddress, kvineConfig.serverPort)

        logger.info("kvine renju inference service connected.")

        val sessionRepository = SessionRepository(dbConnection = dbConnection)

        val botContext = BotContext(botConfig, dbConnection, kvineClient, sessionRepository)

        val eventManager = ReactiveEventManager()

        eventManager.on<SlashCommandInteractionEvent>()
            .filter { it.isFromGuild && !it.user.isBot }
            .flatMap { mono { retrieveInteractionContext(botContext, it, it.user, it.guild!!) } }
            .flatMap(::slashCommandRouter)
            .onErrorContinue { error, _ -> leaveLog(SlashCommandInteractionEvent::class, error) }
            .subscribe { leaveLog(it) }

        eventManager.on<MessageReceivedEvent>()
            .filter { it.isFromGuild && !it.author.isBot && (it.message.contentRaw.startsWith(COMMAND_PREFIX) || it.message.mentions.isMentioned(it.jda.selfUser)) }
            .flatMap { mono { retrieveInteractionContext(botContext, it, it.author, it.guild) } }
            .flatMap(::textCommandRouter)
            .onErrorContinue { error, _ -> leaveLog(MessageReceivedEvent::class, error) }
            .subscribe { leaveLog(it) }

        eventManager.on<ButtonInteractionEvent>()
            .filter { it.isFromGuild && !it.user.isBot }
            .flatMap { mono { retrieveInteractionContext(botContext, it, it.user, it.guild!!) } }
            .flatMap(::buttonInteractionRouter)
            .onErrorContinue { error, _ -> leaveLog(ButtonInteractionEvent::class, error) }
            .subscribe { leaveLog(it) }

        eventManager.on<SelectMenuInteractionEvent>()
            .filter { it.isFromGuild && !it.user.isBot }
            .flatMap { mono { retrieveInteractionContext(botContext, it, it.user, it.guild!!) } }
            .flatMap(::buttonInteractionRouter)
            .onErrorContinue { error, _ -> leaveLog(SelectMenuInteractionEvent::class, error) }
            .subscribe { leaveLog(it) }

        eventManager.on<MessageReactionAddEvent>()
            .filter { it.isFromGuild && !(it.user?.isBot ?: true) }
            .flatMap { mono { retrieveInteractionContext(botContext, it, it.user!!, it.guild) } }
            .flatMap(::reactionRouter)
            .onErrorContinue { error, _ -> leaveLog(MessageReceivedEvent::class, error) }
            .subscribe { leaveLog(it) }

        eventManager.on<GuildJoinEvent>()
            .flatMap { guildJoinRouter(botContext, it) }
            .onErrorContinue { error, _ -> leaveLog(GuildJoinEvent::class, error) }
            .subscribe { println("join $it") }

        eventManager.on<GuildLeaveEvent>()
            .flatMap { mono { GuildProfileRepository.retrieveGuild(botContext.dbConnection, DISCORD_PLATFORM_ID, it.guild.extractId()) } }
            .onErrorContinue { error, _ -> leaveLog(GuildLeaveEvent::class, error) }
            .subscribe { guild -> guild.forEach { logger.info("leave $it") } }

        eventManager.on<ReadyEvent>()
            .subscribe { logger.info("jda ready, complete loading.") }

        eventManager.on<ShutdownEvent>()
            .subscribe { logger.info("jda shutdown.") }

        logger.info("reactive event manager ready.")

        val jda = JDABuilder.createLight(Token.fromEnv().token)
            .setEventManager(eventManager)
            .setActivity(Activity.playing("/help or ${COMMAND_PREFIX}help or @GomokuBot"))
            .setStatus(OnlineStatus.ONLINE)
            .setEnabledIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS)
            .build()

        scheduleRoutines(logger, botContext, jda)
    }

}

val logger = getLogger<GomokuBot>()

fun main() {
    logger.info(ASCII_LOGO)

    runCatching { GomokuBot.launch() }
        .onSuccess { logger.info("gomokubot ready.") }
        .onFailure { logger.error(it.stackTraceToString()) }
}
