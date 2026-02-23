package discord.interact

import core.BotContext
import core.assets.Channel
import core.database.repositories.ChannelProfileRepository
import core.session.SessionManager
import core.session.entities.ChannelConfig
import discord.assets.*
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.events.Event
import reactor.core.publisher.Mono
import utils.assets.LinuxTime

data class InternalInteractionContext<out E : Event> (
    override val bot: BotContext,
    override val discordConfig: DiscordConfig,
    override val event: E,
    override val guild: Channel,
    override val config: ChannelConfig,
    override val emittedTime: LinuxTime,
    override val source: String
) : InteractionContext<E> {

    companion object {

        fun <E: Event> fromJDAEvent(bot: BotContext, discordConfig: DiscordConfig, event: E, jdaChannel: JDAChannel): Mono<InternalInteractionContext<E>> = mono {
            val guild = ChannelProfileRepository.retrieveOrInsertChannel(bot.dbConnection, DISCORD_PLATFORM_ID, jdaChannel.extractId()) {
                jdaChannel.extractProfile()
            }

            InternalInteractionContext(
                bot = bot,
                discordConfig = discordConfig,
                event = event,
                guild = guild,
                config = SessionManager.retrieveChannelConfig(bot.sessions, guild),
                emittedTime = LinuxTime.now(),
                source = event.abbreviation()
            )
        }
    }
}
