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
import core.interact.reports.ErrorReport
import core.interact.reports.InteractionReport
import core.session.SessionManager
import core.session.SessionRepository
import dev.minn.jda.ktx.coroutines.await
import discord.assets.*
import discord.interact.DiscordConfig
import discord.interact.GuildManager
import discord.interact.InteractionContext
import discord.route.*
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.Permission
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
import utils.assets.LinuxTime
import utils.lang.and
import utils.log.getLogger

private data class PostgreSQLConfig(val serverURL: String) {
    companion object {
        fun fromEnv(): PostgreSQLConfig = PostgreSQLConfig(
            serverURL = System.getenv("GOMOKUBOT_DB_URL")
        )
    }
}

private data class KvineConfig(val serverAddress: String, val serverPort: Int) {
    companion object {
        fun fromEnv(): KvineConfig = KvineConfig(
            serverAddress = System.getenv("GOMOKUBOT_KVINE_ADDRESS"),
            serverPort = System.getenv("GOMOKUBOT_KVINE_PORT").toInt()
        )
    }
}

object DiscordConfigBuilder {
    fun fromEnv(): DiscordConfig = DiscordConfig(
        token = System.getenv("GOMOKUBOT_DISCORD_TOKEN"),
        officialServerId = GuildId(System.getenv("GOMOKUBOT_DISCORD_OFFICIAL_SERVER_ID").toLong()),
        archiveChannelId = ChannelId(System.getenv("GOMOKUBOT_DISCORD_ARCHIVE_CHANNEL_ID").toLong()),
        testerRoleId = System.getenv("GOMOKUBOT_DISCORD_TESTER_ROLE_ID").toLong()
    )
}

fun leaveLog(report: InteractionReport) {
    when (report) {
        is ErrorReport -> logger.error(report.toString())
        else -> logger.info(report.toString())
    }
}

private suspend fun <E : Event> buildInteractionContext(bot: BotContext, discordConfig: DiscordConfig, event: E, jdaUser: JDAUser, jdaGuild: JDAGuild): InteractionContext<E> {
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
        emittedTime = LinuxTime.now()
    )
}

object GomokuBot {

    fun launch() {
        val botConfig = BotConfig()

        val postgresqlConfig = PostgreSQLConfig.fromEnv()
        val kvineConfig = KvineConfig.fromEnv()

        val discordConfig = DiscordConfigBuilder.fromEnv()

        val caches = LocalCaches()

        val dbConnection = runBlocking {
            DatabaseManager.newConnectionFrom(postgresqlConfig.serverURL, caches)
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
                GatewayIntent.MESSAGE_CONTENT,
            )
            .build()

        eventManager.on<ReadyEvent>()
            .subscribe { logger.info("jda ready, complete loading.") }

        eventManager.on<ShutdownEvent>()
            .subscribe { logger.info("jda shutdown.") }

        Flux.merge(
            eventManager.on<SlashCommandInteractionEvent>()
                .filter { it.isFromGuild && it.channel.type == ChannelType.TEXT && !it.user.isBot }
                .flatMap { mono { buildInteractionContext(botContext, discordConfig, it, it.user, it.guild!!) } }
                .flatMap(::slashCommandRouter),

            eventManager.on<MessageReceivedEvent>()
                .filter {
                    it.isFromGuild
                            && it.channel.type == ChannelType.TEXT
                            && !it.author.isBot
                            && (it.message.contentRaw.startsWith(COMMAND_PREFIX) || it.message.mentions.isMentioned(it.jda.selfUser))
                }
                .flatMap { mono { buildInteractionContext(botContext, discordConfig, it, it.author, it.guild) } }
                .flatMap(::textCommandRouter),

            eventManager.on<ButtonInteractionEvent>()
                .filter { it.isFromGuild && !it.user.isBot }
                .flatMap { mono { buildInteractionContext(botContext, discordConfig, it, it.user, it.guild!!) } }
                .flatMap(::buttonInteractionRouter),

            eventManager.on<SelectMenuInteractionEvent>()
                .filter { it.isFromGuild && !it.user.isBot }
                .flatMap { mono { buildInteractionContext(botContext, discordConfig, it, it.user, it.guild!!) } }
                .flatMap(::buttonInteractionRouter),

            eventManager.on<MessageReactionAddEvent>()
                .filter {
                    it.isFromGuild
                            && it.channel.type == ChannelType.TEXT
                            && NAVIGATION_EMOJIS.contains(it.emoji)
                            && !(it.user?.isBot ?: false)
                }
                .flatMap { mono { buildInteractionContext(botContext, discordConfig, it, it.user!!, it.guild) } }
                .flatMap(::reactionRouter),

            eventManager.on<MessageReactionRemoveEvent>()
                .filter {
                    it.isFromGuild
                            && it.channel.type == ChannelType.TEXT
                            && NAVIGATION_EMOJIS.contains(it.emoji)
                            && !GuildManager.lookupPermission(it.channel.asTextChannel(), Permission.MESSAGE_MANAGE)
                            && !(it.user?.isBot ?: false)
                }
                .flatMap { mono {
                    it and it.guild
                        .retrieveMemberById(it.userId)
                        .mapToResult()
                        .map { maybeMember -> maybeMember.map { it.user } }
                        .await()
                } }
                .filter { (_, maybeUser) -> maybeUser.isSuccess && !maybeUser.get().isBot }
                .flatMap { (event, user) -> mono { buildInteractionContext(botContext, discordConfig, event, user.get(), event.guild) } }
                .flatMap(::reactionRouter),

            eventManager.on<GuildJoinEvent>()
                .flatMap { guildJoinRouter(botContext, it) },

            eventManager.on<GuildLeaveEvent>()
                .flatMap { guildLeaveRouter(botContext, it) },

            scheduleRoutines(botContext, discordConfig, jda)
        )
            .onErrorContinue { error, _ -> logger.error(error.stackTraceToString()) }
            .subscribe(::leaveLog)

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
