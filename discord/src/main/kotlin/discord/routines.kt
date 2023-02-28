package discord

import core.BotContext
import core.database.repositories.AnnounceRepository
import core.interact.commands.CommandResult
import core.interact.commands.ExpireGameCommand
import core.interact.commands.ExpireRequestCommand
import core.interact.commands.InternalCommand
import core.interact.message.MonoPublisherSet
import core.session.SessionManager
import core.session.entities.GuildSession
import discord.interact.DiscordConfig
import discord.interact.message.DiscordMessageProducer
import discord.interact.message.MessageCreateAdaptor
import discord.interact.message.MessageEditAdaptor
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.reactor.asFlux
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import reactor.core.publisher.Flux
import utils.lang.schedule

private suspend fun executeCommand(command: InternalCommand, bot: BotContext, guildSession: GuildSession, channel: MessageChannel): CommandResult =
    command.execute(
        bot = bot,
        config = guildSession.config,
        guild = guildSession.guild,
        producer = DiscordMessageProducer,
        publisher = MonoPublisherSet(
            publisher = { msg -> MessageCreateAdaptor(channel.sendMessage(msg.buildCreate())) },
            editGlobal = { ref -> { msg -> MessageEditAdaptor(channel.editMessageById(ref.id.idLong, msg.buildEdit())) } }
        )
    )

fun scheduleExpireRoutines(bot: BotContext, discordConfig: DiscordConfig, jda: JDA): Flux<CommandResult> {
    val expireGameSessionFlow = schedule(bot.config.gameExpireCycle) {
        SessionManager.cleanExpiredGameSession(bot.sessions).forEach { (_, guildSession, _, session) ->
            val maybeMessage = SessionManager.viewHeadMessage(bot.sessions, session.messageBufferKey)

            val maybeGuild = jda.getGuildById(guildSession.guild.givenId.idLong)
            val maybeChannel = maybeMessage?.let { maybeGuild?.getTextChannelById(it.channelId.idLong) }

            val command = ExpireGameCommand(
                guildSession = guildSession,
                session = session,
                channelAvailable = maybeGuild != null && maybeChannel != null
            )

            val result = executeCommand(command, bot, guildSession, maybeChannel!!)

            emit(result)
        }

        SessionManager.cleanEmptySessions(bot.sessions)
    }

    val expireRequestSessionFlow = schedule(bot.config.requestExpireCycle) {
        SessionManager.cleanExpiredRequestSessions(bot.sessions).forEach { (_, guildSession, _, session) ->
            val maybeMessage = SessionManager.viewHeadMessage(bot.sessions, session.messageBufferKey)

            val maybeGuild = jda.getGuildById(guildSession.guild.givenId.idLong)
            val maybeChannel = maybeMessage?.let { jda.getTextChannelById(it.channelId.idLong) }

            val command = ExpireRequestCommand(
                guildSession = guildSession,
                session = session,
                channelAvailable = maybeGuild != null && maybeChannel != null,
                messageAvailable = maybeMessage != null
            )

            val result = executeCommand(command, bot, guildSession, maybeChannel!!)

            emit(result)
        }
    }

    return merge(expireGameSessionFlow, expireRequestSessionFlow).asFlux()
}

fun scheduleUpdateRoutines(bot: BotContext): Flux<Unit> {

    val expireNavigateFlow = schedule<Unit>(bot.config.navigatorExpireCycle) {
        SessionManager.cleanExpiredNavigators(bot.sessions)
    }

    val announceFlow = schedule<Unit>(bot.config.announceUpdateCycle) {
        AnnounceRepository.updateAnnounceCache(bot.dbConnection)
    }

    return merge(expireNavigateFlow, announceFlow).asFlux()
}
