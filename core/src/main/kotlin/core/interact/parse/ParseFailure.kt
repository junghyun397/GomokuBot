package core.interact.parse

import core.assets.Guild
import core.assets.User
import core.interact.Order
import core.interact.i18n.LanguageContainer
import core.interact.message.MessagePublisher
import core.interact.message.MessagingService
import core.interact.reports.CommandReport
import core.session.entities.GuildConfig
import utils.lang.tuple
import utils.structs.IO

class ParseFailure<A, B>(
    val name: String,
    val comment: String,
    val guild: Guild,
    val user: User,
    private val onFailure: (MessagingService<A, B>, MessagePublisher<A, B>, LanguageContainer) -> IO<List<Order>>
) {

    fun notice(config: GuildConfig, service: MessagingService<A, B>, publisher: MessagePublisher<A, B>): Result<Pair<IO<List<Order>>, CommandReport>> =
        Result.success(tuple(onFailure(service, publisher, config.language.container), this.asCommandReport()))

}

fun <A, B> CommandParser.asParseFailure(comment: String, guild: Guild, user: User, onFailure: (MessagingService<A, B>, MessagePublisher<A, B>, LanguageContainer) -> IO<List<Order>>) =
    ParseFailure(this.name, comment, guild, user, onFailure)

fun ParseFailure<*, *>.asCommandReport() =
    CommandReport("PARSE-FAILURE-${this.name}", this.comment, this.guild, this.user)
