package discord

import core.BotConfig
import core.BotContext
import core.assets.ChannelId
import core.assets.SubChannelId
import core.database.DatabaseManager
import core.database.LocalCaches
import core.database.repositories.AnnounceRepository
import core.interact.reports.ErrorReport
import core.interact.reports.Report
import core.mintaka.MintakaProvider
import core.mintaka.MintakaServer
import core.session.MessageManager
import core.session.SessionPool
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.CoroutineEventManager
import dev.minn.jda.ktx.events.listener
import discord.assets.ASCII_SPLASH
import discord.assets.COMMAND_PREFIX
import discord.assets.NAVIGATION_EMOJIS
import discord.interact.ChannelManager
import discord.interact.DiscordConfig
import discord.interact.InternalInteractionContext
import discord.interact.UserInteractionContext
import discord.route.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.events.session.ShutdownEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import utils.log.getLogger
import kotlin.system.exitProcess

fun postgresqlUrlFromEnv(): String = System.getenv("GOMOKUBOT_DB_URL")

fun mintakaServerFromEnv(): MintakaServer = MintakaServer(
    url = System.getenv("GOMOKUBOT_MINTAKA_URL"),
    password = System.getenv("GOMOKUBOT_MINTAKA_API_PASSWORD")
)

fun discordConfigFromEnv(): DiscordConfig = DiscordConfig(
    token = System.getenv("GOMOKUBOT_DISCORD_TOKEN"),
    officialServerId = ChannelId(System.getenv("GOMOKUBOT_DISCORD_OFFICIAL_SERVER_ID").toLong()),
    archiveSubChannelId = SubChannelId(System.getenv("GOMOKUBOT_DISCORD_ARCHIVE_CHANNEL_ID").toLong()),
    testerRoleId = System.getenv("GOMOKUBOT_DISCORD_TESTER_ROLE_ID").toLong()
)

fun leaveLog(report: Report) {
    when (report) {
        is ErrorReport -> logger.error(report.buildLog())
        else -> logger.info(report.buildLog())
    }
}

private inline fun <reified E : GenericEvent> JDA.eventFlow(): Flow<E> =
    callbackFlow {
        val events = this@callbackFlow
        val listener = this@eventFlow.listener<E> { event ->
            events.send(event)
        }

        awaitClose { listener.cancel() }
    }

private fun <E> Flow<E>.route(transform: suspend (E) -> Report?): Flow<Report> =
    channelFlow {
        collect { event ->
            launch {
                runCatching {
                    transform(event)
                }.onSuccess { report ->
                    report?.let { send(it) }
                }.onFailure { error ->
                    logger.error(error.stackTraceToString())
                }
            }
        }
    }

object GomokuBot {

    fun launch() {
        val botConfig = BotConfig()

        val postgresqlUrl = postgresqlUrlFromEnv()
        val mintakaServer = mintakaServerFromEnv()
        val discordConfig = discordConfigFromEnv()

        val caches = LocalCaches()

        val dbConnection = runBlocking {
            DatabaseManager.newConnectionFrom(postgresqlUrl, caches)
                .also { connection ->
                    DatabaseManager.initCaches(connection)
                }
        }

        logger.info("postgresql database connected.")

        runBlocking {
            val result = MintakaProvider.validateServer(mintakaServer)

            if (!result) {
                logger.error("mintaka server is not available.")
                exitProcess(1)
            }
        }

        logger.info("mintaka server connected.")

        val sessionPool = SessionPool(dbConnection = dbConnection)

        val botContext = BotContext(botConfig, dbConnection, mintakaServer, sessionPool)

        val eventManager = CoroutineEventManager()

        val jda = JDABuilder.createLight(discordConfig.token)
            .useSharding(0, 1)
            .setEventManager(eventManager)
            .setActivity(Activity.customStatus("/help or ${COMMAND_PREFIX}help or @GomokuBot"))
            .setStatus(OnlineStatus.ONLINE)
            .setEnabledIntents(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.MESSAGE_CONTENT,
            )
            .build()

        jda.listener<ReadyEvent> {
            logger.info("jda ready, complete loading.")
        }

        jda.listener<ShutdownEvent> {
            logger.info("jda shutdown.")
        }

        jda.listener<CommandAutoCompleteInteractionEvent> {
            if (!it.isFromGuild || it.user.isBot)
                return@listener

            commandAutoCompleteRouter(it)
        }

        val commandFlow: Flow<Report> = merge(
            jda.eventFlow<SlashCommandInteractionEvent>()
                .filter { it.isFromGuild && !it.user.isBot }
                .route {
                    slashCommandRouter(UserInteractionContext.fromJDAEvent(botContext, discordConfig, it, it.user, it.guild!!))
                },

            jda.eventFlow<MessageReceivedEvent>()
                .filter {
                    it.isFromGuild
                            && !it.author.isBot
                            && (it.message.contentRaw.startsWith(COMMAND_PREFIX) ||
                                (!it.message.mentions.mentionsEveryone()
                                            && it.message.mentions.membersBag.size == 1
                                            && it.message.mentions.isMentioned(it.jda.selfUser)
                                )
                            )
                }
                .route {
                    textCommandRouter(UserInteractionContext.fromJDAEvent(botContext, discordConfig, it, it.author, it.guild))
                },

            jda.eventFlow<ButtonInteractionEvent>()
                .filter { it.isFromGuild && !it.user.isBot }
                .route {
                    buttonInteractionRouter(UserInteractionContext.fromJDAEvent(botContext, discordConfig, it, it.user, it.guild!!))
                },

            jda.eventFlow<StringSelectInteractionEvent>()
                .filter { it.isFromGuild && !it.user.isBot }
                .route {
                    buttonInteractionRouter(UserInteractionContext.fromJDAEvent(botContext, discordConfig, it, it.user, it.guild!!))
                },

            jda.eventFlow<MessageReactionAddEvent>()
                .filter {
                    it.isFromGuild
                            && it.userIdLong != jda.selfUser.idLong
                            && it.messageAuthorIdLong == jda.selfUser.idLong
                            && it.channel.type == ChannelType.TEXT
                            && NAVIGATION_EMOJIS.contains(it.emoji)
                            && !(it.user?.isBot ?: false)
                }
                .route {
                    reactionRouter(UserInteractionContext.fromJDAEvent(botContext, discordConfig, it, it.user!!, it.guild))
                },

            jda.eventFlow<MessageReactionRemoveEvent>()
                .filter {
                    it.isFromGuild
                            && it.userIdLong != jda.selfUser.idLong
                            && !(it.user?.isBot ?: false)
                            && it.channel.type == ChannelType.TEXT
                            && NAVIGATION_EMOJIS.contains(it.emoji)
                            && !ChannelManager.lookupPermission(it.channel.asGuildMessageChannel(), Permission.MESSAGE_MANAGE)
                }
                .route {
                    val user = it.guild
                        .retrieveMemberById(it.userId)
                        .mapToResult()
                        .map { maybeMember -> maybeMember.map(Member::getUser) }
                        .await()

                    if (user.isSuccess && !user.get().isBot) {
                        reactionRouter(UserInteractionContext.fromJDAEvent(botContext, discordConfig, it, user.get(), it.guild))
                    } else {
                        null
                    }
                },

            jda.eventFlow<GuildJoinEvent>()
                .route { event ->
                    channelJoinRouter(InternalInteractionContext.fromJDAEvent(botContext, discordConfig, event, event.guild))
                },

            jda.eventFlow<GuildLeaveEvent>()
                .route { event ->
                    channelLeaveRouter(InternalInteractionContext.fromJDAEvent(botContext, discordConfig, event, event.guild))
                },

            scheduleGameExpiration(botContext, discordConfig, jda),
            scheduleRequestExpiration(botContext, discordConfig, jda),

            routine(botConfig.navigatorExpireChecks) {
                val expires = MessageManager.cleanExpiredNavigators(sessionPool)

                "cleaned $expires expired navigators"
            },

            routine(botConfig.announceUpdateChecks) {
                val announces = AnnounceRepository.fetchAnnounces(dbConnection)
                val updated = announces.size - dbConnection.localCaches.announceCache.size

                "updated $updated announces"
            }
        )

        eventManager.launch {
            commandFlow.collect { report -> leaveLog(report) }
        }

        logger.info("coroutine event manager ready.")

        ChannelManager.initGlobalCommand(jda)

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
