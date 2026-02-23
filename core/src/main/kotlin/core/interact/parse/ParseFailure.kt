package core.interact.parse

import arrow.core.raise.Effect
import core.assets.Channel
import core.assets.User
import core.interact.Order
import core.interact.i18n.LanguageContainer
import core.interact.message.MessagePublisher
import core.interact.message.MessagingService
import core.interact.reports.CommandReport
import core.session.entities.ChannelConfig
import utils.lang.tuple

class ParseFailure<A, B>(
    val name: String,
    val comment: String,
    val guild: Channel,
    val user: User,
    private val onFailure: (MessagingService<A, B>, MessagePublisher<A, B>, LanguageContainer) -> Effect<Nothing, List<Order>>
) {

    fun notice(config: ChannelConfig, service: MessagingService<A, B>, publisher: MessagePublisher<A, B>): Result<Pair<Effect<Nothing, List<Order>>, CommandReport>> =
        Result.success(tuple(onFailure(service, publisher, config.language.container), this.asCommandReport()))

}

fun <A, B> CommandParser.asParseFailure(comment: String, guild: Channel, user: User, onFailure: (MessagingService<A, B>, MessagePublisher<A, B>, LanguageContainer) -> Effect<Nothing, List<Order>>) =
    ParseFailure(this.name, comment, guild, user, onFailure)

fun ParseFailure<*, *>.asCommandReport() =
    CommandReport("PARSE-FAILURE-${this.name}", this.comment, this.guild, this.user)
