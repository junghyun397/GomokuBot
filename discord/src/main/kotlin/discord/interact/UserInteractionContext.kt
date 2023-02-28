package discord.interact

import core.BotContext
import core.assets.Guild
import core.assets.User
import core.database.repositories.GuildProfileRepository
import core.database.repositories.UserProfileRepository
import core.session.SessionManager
import core.session.entities.GuildConfig
import discord.assets.*
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.events.Event
import reactor.core.publisher.Mono
import utils.assets.LinuxTime

data class UserInteractionContext<out E : Event>(
    override val bot: BotContext,
    override val discordConfig: DiscordConfig,
    override val event: E,
    val user: User,
    override val guild: Guild,
    override val config: GuildConfig,
    override val emittedTime: LinuxTime
) : InteractionContext<E> {

    companion object {

        fun <E: Event> fromJDAEvent(bot: BotContext, discordConfig: DiscordConfig, event: E, jdaUser: JDAUser, jdaGuild: JDAGuild): Mono<UserInteractionContext<E>> = mono {
            val user = UserProfileRepository.retrieveOrInsertUser(bot.dbConnection, DISCORD_PLATFORM_ID, jdaUser.extractId()) {
                jdaUser.extractProfile()
            }

            val guild = GuildProfileRepository.retrieveOrInsertGuild(bot.dbConnection, DISCORD_PLATFORM_ID, jdaGuild.extractId()) {
                jdaGuild.extractProfile()
            }

            UserInteractionContext(
                bot = bot,
                discordConfig = discordConfig,
                event = event,
                user = user,
                guild = guild,
                config = SessionManager.retrieveGuildConfig(bot.sessions, guild),
                emittedTime = LinuxTime.now()
            )
        }

    }

}
