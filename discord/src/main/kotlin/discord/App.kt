package discord

import club.minnced.jda.reactor.ReactiveEventManager
import club.minnced.jda.reactor.on
import core.BotConfig
import core.BotContext
import core.assets.ChannelId
import core.assets.GuildId
import core.database.DatabaseManager
import core.database.LocalCaches
import core.database.repositories.GuildProfileRepository
import core.database.repositories.UserProfileRepository
import core.inference.KvineClient
import core.interact.reports.InteractionReport
import core.session.SessionManager
import core.session.SessionRepository
import discord.assets.*
import discord.interact.DiscordConfig
import discord.interact.GuildManager
import discord.interact.InteractionContext
import discord.route.*
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.ChannelType
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
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import reactor.core.publisher.Flux
import reactor.util.function.Tuple2
import utils.assets.LinuxTime
import utils.log.getLogger
import utils.structs.forEach

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

object DiscordConfigBuilder {
    fun fromEnv() = DiscordConfig(
        token = System.getenv("GOMOKUBOT_DISCORD_TOKEN"),
        officialServerId = GuildId(System.getenv("GOMOKUBOT_DISCORD_OFFICIAL_SERVER_ID").toLong()),
        archiveChannelId = ChannelId(System.getenv("GOMOKUBOT_DISCORD_ARCHIVE_CHANNEL_ID").toLong()),
        testerRoleId = System.getenv("GOMOKUBOT_DISCORD_TESTER_ROLE_ID").toLong()
    )
}

private inline fun <reified E : Event, R : InteractionReport> leaveLog(tuple: Tuple2<InteractionContext<E>, Result<R>>) =
    tuple.t2
        .onSuccess{
            logger.info("${E::class.simpleName} ${tuple.t1.guild} " +
                    "T${(it.terminationTime.timestamp - tuple.t1.emittedTime.timestamp)}ms => $it")
        }
        .onFailure {
            logger.error("${E::class.simpleName} ${tuple.t1.guild} " +
                    "T${(System.currentTimeMillis() - tuple.t1.emittedTime.timestamp)}ms => ${it.stackTraceToString()}")
        }

private suspend inline fun <E : Event> retrieveInteractionContext(bot: BotContext, discordConfig: DiscordConfig, event: E, jdaUser: net.dv8tion.jda.api.entities.User, jdaGuild: net.dv8tion.jda.api.entities.Guild): InteractionContext<E> {
    val user = UserProfileRepository.retrieveOrInsertUser(bot.dbConnection, DISCORD_PLATFORM_ID, jdaUser.extractId()) {
        jdaUser.extractProfile()
    }

    val guild = GuildProfileRepository.retrieveOrInsertGuild(bot.dbConnection, DISCORD_PLATFORM_ID, jdaGuild.extractId()) {
        jdaGuild.extractProfile()
    }

    return InteractionContext(
        bot = bot,
        discordConfig = discordConfig,
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

        val discordConfig = DiscordConfigBuilder.fromEnv()

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

        val jda = JDABuilder.createLight(discordConfig.token)
            .useSharding(0, 1)
            .setEventManager(eventManager)
            .setActivity(Activity.playing("/help or ${COMMAND_PREFIX}help or @GomokuBot"))
            .setStatus(OnlineStatus.ONLINE)
            .setEnabledIntents(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.MESSAGE_CONTENT
            )
            .build()

        logger.info("jda ready.")

        Flux.merge(
            eventManager.on<SlashCommandInteractionEvent>()
                .filter { it.isFromGuild && it.channel.type == ChannelType.TEXT && !it.user.isBot }
                .flatMap { mono { retrieveInteractionContext(botContext, discordConfig, it, it.user, it.guild!!) } }
                .flatMap(::slashCommandRouter)
                .doOnNext { leaveLog(it) },

            eventManager.on<MessageReceivedEvent>()
                .filter { it.isFromGuild && it.channel.type == ChannelType.TEXT && !it.author.isBot && (it.message.contentRaw.startsWith(COMMAND_PREFIX) || it.message.mentions.isMentioned(it.jda.selfUser)) }
                .flatMap { mono { retrieveInteractionContext(botContext, discordConfig, it, it.author, it.guild) } }
                .flatMap(::textCommandRouter)
                .doOnNext { leaveLog(it) },

            eventManager.on<ButtonInteractionEvent>()
                .filter { it.isFromGuild && !it.user.isBot }
                .flatMap { mono { retrieveInteractionContext(botContext, discordConfig, it, it.user, it.guild!!) } }
                .flatMap(::buttonInteractionRouter)
                .doOnNext { leaveLog(it) },

            eventManager.on<SelectMenuInteractionEvent>()
                .filter { it.isFromGuild && !it.user.isBot }
                .flatMap { mono { retrieveInteractionContext(botContext, discordConfig, it, it.user, it.guild!!) } }
                .flatMap(::buttonInteractionRouter)
                .doOnNext { leaveLog(it) },

            eventManager.on<MessageReactionAddEvent>()
                .filter { it.isFromGuild && !(it.user?.isBot ?: true) }
                .flatMap { mono { retrieveInteractionContext(botContext, discordConfig, it, it.user!!, it.guild) } }
                .flatMap(::reactionRouter)
                .doOnNext { leaveLog(it) },

            eventManager.on<MessageReactionRemoveEvent>()
                .filter { it.isFromGuild && !(it.user?.isBot ?: true) }
                .flatMap { mono { retrieveInteractionContext(botContext, discordConfig, it, it.user!!, it.guild) } }
                .flatMap(::reactionRouter)
                .doOnNext { leaveLog(it) },

            eventManager.on<GuildJoinEvent>()
                .flatMap { guildJoinRouter(botContext, it) }
                .doOnNext { println("join $it") },

            eventManager.on<GuildLeaveEvent>()
                .flatMap { mono { GuildProfileRepository.retrieveGuild(botContext.dbConnection, DISCORD_PLATFORM_ID, it.guild.extractId()) } }
                .doOnNext { guild -> guild.forEach { logger.info("leave $it") } },

            eventManager.on<ReadyEvent>()
                .doOnNext { logger.info("jda ready, complete loading.") },

            eventManager.on<ShutdownEvent>()
                .doOnNext { logger.info("jda shutdown.") },

            scheduleRoutines(botContext, discordConfig, jda)
        )
            .onErrorContinue { error, _ -> logger.error(error.stackTraceToString()) }
            .subscribe()

        logger.info("reactive event manager ready.")

        GuildManager.initGlobalCommand(jda)

        logger.info("discord global command uploaded.")
    }

}

val logger = getLogger<GomokuBot>()

fun main() {
    logger.info(ASCII_LOGO)

    runCatching { GomokuBot.launch() }
        .onSuccess { logger.info("gomokubot ready.") }
        .onFailure { logger.error(it.stackTraceToString()) }
}
