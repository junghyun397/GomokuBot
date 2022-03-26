package discord

import club.minnced.jda.reactor.ReactiveEventManager
import club.minnced.jda.reactor.on
import core.BotContext
import core.inference.B3nzeneClient
import core.interact.reports.InteractionReport
import core.session.SessionManager
import core.session.SessionRepository
import discord.interact.InteractionContext
import discord.route.buttonInteractionRouter
import discord.route.guildJoinRouter
import discord.route.slashCommandRouter
import discord.route.textCommandRouter
import discord.utils.ASCII_LOGO
import discord.utils.COMMAND_PREFIX
import discord.utils.extractId
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.ShutdownEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import reactor.util.function.Tuple2
import utils.values.LinuxTime
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

private inline fun <reified E : Event, reified R : InteractionReport> leaveLog(combined: Tuple2<InteractionContext<E>, Result<R>>) =
    getLogger<R>().let { logger ->
        combined.t2.fold(
            onSuccess = {
                logger.info("${E::class.simpleName} (${combined.t1.guildName})/${combined.t1.guild.id} " +
                        "T${(it.terminationTime.timestamp - combined.t1.emittenTime.timestamp)}ms => $it")
            },
            onFailure = {
                logger.error("${E::class.simpleName} (${combined.t1.guildName})/${combined.t1.guild.id} " +
                        "T${(System.currentTimeMillis() - combined.t1.emittenTime.timestamp)}ms => ${it.stackTraceToString()}")
            }
        )
    }

private suspend inline fun <E : Event> retrieveInteractionContext(botContext: BotContext, event: E, guild: Guild) =
    guild.extractId().let {
        InteractionContext(
            botContext = botContext,
            event = event,
            guildConfig = SessionManager.retrieveGuildConfig(botContext.sessionRepository, it),
            guild = it,
            guildName = guild.name,
            emittenTime = LinuxTime()
        )
    }

object GomokuBot {

    fun launch() {
        val logger = getLogger<GomokuBot>()

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

        val botContext = BotContext(databaseConnection, b3nzeneClient, sessionRepository)

        val eventManager = ReactiveEventManager()

        eventManager.on<SlashCommandInteractionEvent>()
            .filter { it.isFromGuild && !it.user.isBot }
            .flatMap { mono { retrieveInteractionContext(botContext, it, it.guild!!) } }
            .flatMap(::slashCommandRouter)
            .doOnNext { leaveLog(it) }
            .subscribe()

        eventManager.on<MessageReceivedEvent>()
            .filter { it.isFromGuild && !it.author.isBot && it.message.contentRaw.startsWith(COMMAND_PREFIX) }
            .flatMap { mono { retrieveInteractionContext(botContext, it, it.guild) } }
            .flatMap(::textCommandRouter)
            .doOnNext { leaveLog(it) }
            .subscribe()

        eventManager.on<ButtonInteractionEvent>()
            .filter { it.isFromGuild && !it.user.isBot }
            .flatMap { mono { retrieveInteractionContext(botContext, it, it.guild!!) } }
            .flatMap(::buttonInteractionRouter)
            .doOnNext { leaveLog(it) }
            .subscribe()

        eventManager.on<GuildJoinEvent>()
            .flatMap { mono { retrieveInteractionContext(botContext, it, it.guild) } }
            .flatMap(::guildJoinRouter)
            .doOnNext { leaveLog(it) }
            .subscribe()

        eventManager.on<GuildLeaveEvent>()
            .doOnNext { logger.info("leave (${it.guild.name})/${it.guild.idLong}") }
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
    val logger = getLogger<GomokuBot>()
    logger.info(ASCII_LOGO)

    val launchResult = runCatching { GomokuBot.launch() }

    launchResult.fold(
        onSuccess = { logger.info("gomokubot ready.") },
        onFailure = { logger.error(it.stackTraceToString()) }
    )
}
