package core.interact.commands

import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.assets.Channel
import core.assets.User
import core.database.DatabaseConnection
import core.database.repositories.ChannelProfileRepository
import core.database.repositories.UserProfileRepository
import core.engine.MintakaServer
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeActionLog
import core.session.SessionPool
import core.session.entities.ChannelConfig
import utils.tuple
import kotlin.time.Instant

class UpdateProfileCommand(
    command: Command,
    private val newUser: User.Human?,
    private val newChannel: Channel?,
) : UnionCommand(command) {

    override val name = "update-profile"

    context(dbConnection: DatabaseConnection, mintakaServer: MintakaServer, sessions: SessionPool, service: PlatformService)
    override suspend fun executeSelf(
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        publishers: PublisherSet,
        emittedTime: Instant,
    ) = runCatching {
        this.newUser?.let {
            UserProfileRepository.upsertUser(it)
        }

        val thenUser = this.newUser ?: user

        this.newChannel?.let {
            ChannelProfileRepository.upsertChannel(it)
        }

        val report = this.writeActionLog(emittedTime, "$user", channel, user)

        val io: Effect<Nothing, Unit> = effect { }

        tuple(io, report, this.newChannel ?: channel, thenUser)
    }

}
