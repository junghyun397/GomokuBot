package discord

import club.minnced.jda.reactor.ReactiveEventManager
import club.minnced.jda.reactor.on
import core.BotConfig
import core.BotContext
import core.assets.ChannelId
import core.assets.GuildId
import core.database.DatabaseManager
import core.database.LocalCaches
import core.database.repositories.AnnounceRepository
import core.inference.ResRenjuClient
import core.interact.commands.CommandResult
import core.interact.reports.ErrorReport
import core.interact.reports.Report
import core.session.SessionManager
import core.session.SessionPool
import dev.minn.jda.ktx.coroutines.await
import discord.assets.ASCII_SPLASH
import discord.assets.COMMAND_PREFIX
import discord.assets.NAVIGATION_EMOJIS
import discord.interact.*
import discord.route.*
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.events.session.ShutdownEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import utils.lang.tuple
import utils.log.getLogger

private data class PostgreSQLConfig(val serverURL: String) {
    companion object {
        fun fromEnv(): PostgreSQLConfig = PostgreSQLConfig(
            serverURL = System.getenv("GOMOKUBOT_DB_URL")
        )
    }
}

private data class ResRenjuConfig(val serverAddress: String, val serverPort: Int) {
    companion object {
        fun fromEnv(): ResRenjuConfig = ResRenjuConfig(
            serverAddress = System.getenv("GOMOKUBOT_RESRENJU_ADDRESS"),
            serverPort = System.getenv("GOMOKUBOT_RESRENJU_PORT").toInt()
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

fun leaveLog(report: Report) {
    when (report) {
        is ErrorReport -> logger.error(report.buildLog())
        else -> logger.info(report.buildLog())
    }
}

fun <T : Event, C : InteractionContext<T>> withContext(context: C, router: (C) -> Mono<CommandResult>): Mono<Tuple2<InteractionContext<T>, CommandResult>> =
    Mono.zip(Mono.just(context), router(context))

object GomokuBot {

    fun launch() {
        val botConfig = BotConfig()

        val postgresqlConfig = PostgreSQLConfig.fromEnv()
        val resRenjuConfig = ResRenjuConfig.fromEnv()

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

        val resRenjuClient = ResRenjuClient
            .connectionFrom(resRenjuConfig.serverAddress, resRenjuConfig.serverPort)

        logger.info("resrenju renju inference service connected.")

        val sessionPool = SessionPool(dbConnection = dbConnection)

        val botContext = BotContext(botConfig, dbConnection, resRenjuClient, sessionPool)

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

        val commandFlux = Flux.merge(
            eventManager.on<SlashCommandInteractionEvent>()
                .filter { it.isFromGuild && it.channel.type == ChannelType.TEXT && !it.user.isBot }
                .flatMap { UserInteractionContext.fromJDAEvent(botContext, discordConfig, it, it.user, it.guild!!) }
                .flatMap(::slashCommandRouter),

            eventManager.on<MessageReceivedEvent>()
                .filter {
                    it.isFromGuild
                            && it.channel.type == ChannelType.TEXT
                            && !it.author.isBot
                            && (it.message.contentRaw.startsWith(COMMAND_PREFIX) ||
                                it.message.mentions.isMentioned(it.jda.selfUser, Message.MentionType.USER))
                }
                .flatMap { UserInteractionContext.fromJDAEvent(botContext, discordConfig, it, it.author, it.guild) }
                .flatMap(::textCommandRouter),

            eventManager.on<ButtonInteractionEvent>()
                .filter { it.isFromGuild && !it.user.isBot }
                .flatMap { UserInteractionContext.fromJDAEvent(botContext, discordConfig, it, it.user, it.guild!!) }
                .flatMap(::buttonInteractionRouter),

            eventManager.on<StringSelectInteractionEvent>()
                .filter { it.isFromGuild && !it.user.isBot }
                .flatMap { UserInteractionContext.fromJDAEvent(botContext, discordConfig, it, it.user, it.guild!!) }
                .flatMap(::buttonInteractionRouter),

            eventManager.on<MessageReactionAddEvent>()
                .filter {
                    it.isFromGuild
                            && it.userIdLong != jda.selfUser.idLong
                            && it.channel.type == ChannelType.TEXT
                            && NAVIGATION_EMOJIS.contains(it.emoji)
                            && !(it.user?.isBot ?: false)
                }
                .flatMap { UserInteractionContext.fromJDAEvent(botContext, discordConfig, it, it.user!!, it.guild) }
                .flatMap(::reactionRouter),

            eventManager.on<MessageReactionRemoveEvent>()
                .filter {
                    it.isFromGuild
                            && it.userIdLong != jda.selfUser.idLong
                            && !(it.user?.isBot ?: false)
                            && it.channel.type == ChannelType.TEXT
                            && NAVIGATION_EMOJIS.contains(it.emoji)
                            && !GuildManager.lookupPermission(it.channel.asTextChannel(), Permission.MESSAGE_MANAGE)
                }
                .flatMap { mono {
                    val user = it.guild
                        .retrieveMemberById(it.userId)
                        .mapToResult()
                        .map { maybeMember -> maybeMember.map { it.user } }
                        .await()

                    tuple(it, user)
                } }
                .filter { (_, maybeUser) -> maybeUser.isSuccess && !maybeUser.get().isBot }
                .flatMap { (event, user) -> UserInteractionContext.fromJDAEvent(botContext, discordConfig, event, user.get(), event.guild) }
                .flatMap(::reactionRouter),

            eventManager.on<GuildJoinEvent>()
                .flatMap { event -> InternalInteractionContext.fromJDAEvent(botContext, discordConfig, event, event.guild) }
                .flatMap(::guildJoinRouter),

            eventManager.on<GuildLeaveEvent>()
                .flatMap { event -> InternalInteractionContext.fromJDAEvent(botContext, discordConfig, event, event.guild) }
                .flatMap(::guildLeaveRouter),

            scheduleGameExpiration(botContext, discordConfig, jda),
            scheduleRequestExpiration(botContext, discordConfig, jda),

            routine(botConfig.navigatorExpireCycle) {
                val expires = SessionManager.cleanExpiredNavigators(sessionPool)

                "cleaned $expires expired navigators"
            },

            routine(botConfig.announceUpdateCycle) {
                val announces = AnnounceRepository.fetchAnnounces(dbConnection)
                val updated = announces.size - dbConnection.localCaches.announceCache.size

                "updated $updated announces"
            }
        )

        commandFlux
            .onErrorContinue { error, _ -> logger.error(error.stackTraceToString()) }
            .subscribe(::leaveLog)

        logger.info("reactive event manager ready.")

        GuildManager.initGlobalCommand(jda)

        logger.info("discord global command uploaded.")
    }

}

val logger = getLogger<GomokuBot>()

fun main() {
    logger.info(ASCII_SPLASH)

    runCatching { GomokuBot.launch() }
        .onSuccess { logger.info("gomokubot ready.") }
        .onFailure { logger.error(it.stackTraceToString()) }
}
