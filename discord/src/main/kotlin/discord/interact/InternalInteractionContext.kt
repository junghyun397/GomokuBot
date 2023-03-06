package discord.interact

import core.BotContext
import core.assets.Guild
import core.database.repositories.GuildProfileRepository
import core.session.SessionManager
import core.session.entities.GuildConfig
import discord.assets.*
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.events.Event
import reactor.core.publisher.Mono
import utils.assets.LinuxTime

data class InternalInteractionContext<out E : Event> (
    override val bot: BotContext,
    override val discordConfig: DiscordConfig,
    override val event: E,
    override val guild: Guild,
    override val config: GuildConfig,
    override val emittedTime: LinuxTime,
    override val source: String
) : InteractionContext<E> {

    companion object {

        fun <E: Event> fromJDAEvent(bot: BotContext, discordConfig: DiscordConfig, event: E, jdaGuild: JDAGuild): Mono<InternalInteractionContext<E>> = mono {
            val guild = GuildProfileRepository.retrieveOrInsertGuild(bot.dbConnection, DISCORD_PLATFORM_ID, jdaGuild.extractId()) {
                jdaGuild.extractProfile()
            }

            InternalInteractionContext(
                bot = bot,
                discordConfig = discordConfig,
                event = event,
                guild = guild,
                config = SessionManager.retrieveGuildConfig(bot.sessions, guild),
                emittedTime = LinuxTime.now(),
                source = event.abbreviation()
            )
        }
    }
}
