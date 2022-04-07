package core.interact.parse

import core.assets.User
import core.interact.Order
import core.interact.i18n.LanguageContainer
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.CommandReport
import core.session.entities.GuildConfig
import utils.structs.IO

class ParseFailure<A, B>(
    val name: String,
    val comment: String,
    val user: User,
    private val onFailure: (MessageProducer<A, B>, MessagePublisher<A, B>, LanguageContainer) -> IO<Order>
) {

    fun notice(guildConfig: GuildConfig, producer: MessageProducer<A, B>, messagePublisher: MessagePublisher<A, B>): Result<Pair<IO<Order>, CommandReport>> =
        Result.success(onFailure(producer, messagePublisher, guildConfig.language.container) to this.asCommandReport())

}

fun <A, B> NamedParser.asParseFailure(comment: String, user: User, onFailure: (MessageProducer<A, B>, MessagePublisher<A, B>, LanguageContainer) -> IO<Order>) =
    ParseFailure(this.name, comment, user, onFailure)

fun ParseFailure<*, *>.asCommandReport() =
    CommandReport("PARSE-FAILURE-${this.name}", this.comment, this.user)
