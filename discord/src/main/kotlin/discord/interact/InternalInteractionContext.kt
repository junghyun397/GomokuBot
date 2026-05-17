package discord.interact

import core.BotContext
import core.assets.Channel
import core.database.repositories.ChannelProfileRepository
import core.session.SessionManager
import core.session.entities.ChannelConfig
import discord.assets.*
import net.dv8tion.jda.api.events.Event
import kotlin.time.Clock
import kotlin.time.Instant

data class InternalInteractionContext<out E : Event> (
    override val bot: BotContext,
    override val discordConfig: DiscordConfig,
    override val event: E,
    override val channel: Channel,
    override val config: ChannelConfig,
    override val emittedTime: Instant,
    override val source: String
) : InteractionContext<E> {

    companion object {

        suspend fun <E: Event> fromJDAEvent(bot: BotContext, discordConfig: DiscordConfig, event: E, jdaChannel: JDAChannel): InternalInteractionContext<E> {
            val channel = ChannelProfileRepository.retrieveOrInsertChannel(bot.dbConnection, DISCORD_PLATFORM_ID, jdaChannel.extractId()) {
                jdaChannel.extractProfile()
            }

            return InternalInteractionContext(
                bot = bot,
                discordConfig = discordConfig,
                event = event,
                channel = channel,
                config = SessionManager.retrieveChannelConfig(bot.sessions, channel),
                emittedTime = Clock.System.now(),
                source = event.abbreviation()
            )
        }
    }
}
