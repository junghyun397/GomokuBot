package core.interact.commands

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.database.repositories.GuildProfileRepository
import core.database.repositories.UserProfileRepository
import core.interact.Order
import core.interact.emptyOrders
import core.interact.message.MessagingService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.entities.GuildConfig
import utils.lang.tuple

class UpdateProfileCommand(
    command: Command,
    private val newUser: Option<User>,
    private val newGuild: Option<Guild>,
) : UnionCommand(command) {

    override val name = "update-profile"

    override suspend fun <A, B> executeSelf(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        service: MessagingService<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>
    ) = runCatching {
        this.newUser.onSome {
            UserProfileRepository.upsertUser(bot.dbConnection, it)
        }

        val thenUser = this.newUser.getOrElse { user }

        this.newGuild.onSome {
            GuildProfileRepository.upsertGuild(bot.dbConnection, it)
        }

        val thenGuild = this.newGuild.getOrElse { guild }

        val io: Effect<Nothing, List<Order>> = effect { emptyOrders }

        val report = this.writeCommandReport("succeed", guild, user)

        tuple(io, report, thenGuild, thenUser)
    }

}
