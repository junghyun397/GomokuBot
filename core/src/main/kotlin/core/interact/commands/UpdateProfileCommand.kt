package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.database.repositories.GuildProfileRepository
import core.database.repositories.UserProfileRepository
import core.interact.emptyOrders
import core.interact.message.MessageProducer
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.entities.GuildConfig
import utils.lang.tuple
import utils.structs.IO
import utils.structs.Option
import utils.structs.forEach
import utils.structs.orElseGet

class UpdateProfileCommand(
    command: Command,
    private val newUser: Option<User>,
    private val newGuild: Option<Guild>,
) : UnionCommand(command) {

    override val name = "update-profile"

    override val responseFlag = this.command.responseFlag

    override suspend fun <A, B> executeSelf(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>
    ) = runCatching {
        this.newUser.forEach {
            UserProfileRepository.upsertUser(bot.dbConnection, it)
        }

        val thenUser = this.newUser.orElseGet { user }

        this.newGuild.forEach {
            GuildProfileRepository.upsertGuild(bot.dbConnection, it)
        }

        val thenGuild = this.newGuild.orElseGet { guild }

        val io = IO.unit { emptyOrders }

        val report = this.writeCommandReport("succeed", guild, user)

        tuple(io, report, thenGuild, thenUser)
    }

}
