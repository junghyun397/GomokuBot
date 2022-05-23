package discord

import club.minnced.jda.reactor.ReactiveEventManager
import club.minnced.jda.reactor.on
import core.BotConfig
import core.BotContext
import core.assets.Guild
import core.inference.B3nzeneClient
import core.interact.reports.InteractionReport
import core.session.SessionManager
import core.session.SessionRepository
import discord.assets.ASCII_LOGO
import discord.assets.COMMAND_PREFIX
import discord.assets.extractGuild
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

@JvmInline
private value class Token(val token: String) {
    companion object {
        fun fromEnv() = Token(token = System.getenv("GOMOKUBOT_DISCORD_TOKEN"))
    }
}

@JvmInline
private value class MySQLConfig(val serverURL: String) {
    companion object {
        fun fromEnv() = MySQLConfig(
            serverURL = System.getenv("GOMOKUBOT_DB_URL"),
        )
    }
}

private class B3nzeneConfig(val serverAddress: String, val serverPort: Int) {
    companion object {
        fun fromEnv() = B3nzeneConfig(
            serverAddress = System.getenv("GOMOKUBOT_B3NZENE_ADDRESS"),
            serverPort = System.getenv("GOMOKUBOT_B3NZENE_PORT").toInt()
        )
    }
}

private inline fun <reified E : Event, R : InteractionReport> leaveLog(combined: Tuple2<InteractionContext<E>, Result<R>>) =
    combined.t2.fold(
        onSuccess = {
            logger.info("${E::class.simpleName} ${combined.t1.guild} " +
                    "T${(it.terminationTime.timestamp - combined.t1.emittenTime.timestamp)}ms => $it")
        },
        onFailure = {
            logger.error("${E::class.simpleName} ${combined.t1.guild} " +
                    "T${(System.currentTimeMillis() - combined.t1.emittenTime.timestamp)}ms => ${it.stackTraceToString()}")
        }
    )

private suspend inline fun <E : Event> retrieveInteractionContext(botContext: BotContext, event: E, guild: Guild) =
    InteractionContext(
        bot = botContext,
        event = event,
        guild = guild,
        config = SessionManager.retrieveGuildConfig(botContext.sessionRepository, guild.id),
        emittenTime = LinuxTime()
    )

object GomokuBot {

    fun launch() {
        val botConfig = BotConfig()

        val mySQLConfig = MySQLConfig.fromEnv()
        val b3nzeneConfig = B3nzeneConfig.fromEnv()

        val databaseConnection = runBlocking {
            core.database.DatabaseConnection
                .connectionFrom(mySQLConfig.serverURL)
        }
        logger.info("mysql database connected.")

        val b3nzeneClient = B3nzeneClient
            .connectionFrom(b3nzeneConfig.serverAddress, b3nzeneConfig.serverPort)
        logger.info("b3nzene inference service connected.")

        val sessionRepository = SessionRepository(databaseConnection = databaseConnection)

        val botContext = BotContext(botConfig, databaseConnection, b3nzeneClient, sessionRepository)

        val eventManager = ReactiveEventManager()

        eventManager.on<SlashCommandInteractionEvent>()
            .filter { it.isFromGuild && !it.user.isBot }
            .flatMap { mono { retrieveInteractionContext(botContext, it, it.guild!!.extractGuild()) } }
            .flatMap(::slashCommandRouter)
            .subscribe { leaveLog(it) }

        eventManager.on<MessageReceivedEvent>()
            .filter { it.isFromGuild && !it.author.isBot && (it.message.contentRaw.startsWith(COMMAND_PREFIX) || it.message.isMentioned(it.jda.selfUser)) }
            .flatMap { mono { retrieveInteractionContext(botContext, it, it.guild.extractGuild()) } }
            .flatMap(::textCommandRouter)
            .subscribe { leaveLog(it) }

        eventManager.on<ButtonInteractionEvent>()
            .filter { it.isFromGuild && !it.user.isBot }
            .flatMap { mono { retrieveInteractionContext(botContext, it, it.guild!!.extractGuild()) } }
            .flatMap(::buttonInteractionRouter)
            .subscribe { leaveLog(it) }

        eventManager.on<SelectMenuInteractionEvent>()
            .filter { it.isFromGuild && !it.user.isBot }
            .flatMap { mono { retrieveInteractionContext(botContext, it, it.guild!!.extractGuild()) } }
            .flatMap(::buttonInteractionRouter)
            .subscribe { leaveLog(it) }

        eventManager.on<MessageReactionAddEvent>()
            .filter { it.isFromGuild && !(it.user?.isBot ?: true) }
            .flatMap { mono { retrieveInteractionContext(botContext, it, it.guild.extractGuild()) } }
            .flatMap(::reactionRouter)
            .subscribe { leaveLog(it) }

        eventManager.on<GuildJoinEvent>()
            .flatMap { mono { retrieveInteractionContext(botContext, it, it.guild.extractGuild()) } }
            .flatMap(::guildJoinRouter)
            .subscribe { leaveLog(it) }

        eventManager.on<GuildLeaveEvent>()
            .subscribe { logger.info("leave ${it.guild.extractGuild()}") }

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

        scheduleCleaner(botContext, jda)
    }

}

val logger = getLogger<GomokuBot>()

fun main() {
    logger.info(ASCII_LOGO)

    val launchResult = runCatching { GomokuBot.launch() }

    launchResult.fold(
        onSuccess = { logger.info("gomokubot ready.") },
        onFailure = { logger.error(it.stackTraceToString()) }
    )
}
