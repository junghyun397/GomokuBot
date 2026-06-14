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
import utils.tuple

class ParseFailure(
    val name: String,
    val comment: String,
    val channel: Channel,
    val user: User.Human,
    private val onFailure: (MessagingService, MessagePublisher, LanguageContainer) -> Effect<Nothing, List<Order>>
) {

    fun notice(config: ChannelConfig, service: MessagingService, publisher: MessagePublisher): Result<Pair<Effect<Nothing, List<Order>>, CommandReport>> =
        Result.success(tuple(onFailure(service, publisher, config.language.container), this.asCommandReport()))

}

fun CommandParser.asParseFailure(comment: String, channel: Channel, user: User.Human, onFailure: (MessagingService, MessagePublisher, LanguageContainer) -> Effect<Nothing, List<Order>>) =
    ParseFailure(this.name, comment, channel, user, onFailure)

fun ParseFailure.asCommandReport() =
    CommandReport("PARSE-FAILURE-${this.name}", this.comment, this.channel, this.user)
