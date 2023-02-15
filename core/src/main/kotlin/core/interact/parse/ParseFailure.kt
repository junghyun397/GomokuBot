package core.interact.parse

import core.assets.Guild
import core.assets.User
import core.interact.Order
import core.interact.i18n.LanguageContainer
import core.interact.message.MessageProducer
import core.interact.message.MessagePublisher
import core.interact.reports.CommandReport
import core.session.entities.GuildConfig
import utils.lang.tuple
import utils.structs.IO

class ParseFailure<A, B>(
    val name: String,
    val comment: String,
    val guild: Guild,
    val user: User,
    private val onFailure: (MessageProducer<A, B>, MessagePublisher<A, B>, LanguageContainer) -> IO<List<Order>>
) {

    fun notice(config: GuildConfig, producer: MessageProducer<A, B>, publisher: MessagePublisher<A, B>): Result<Pair<IO<List<Order>>, CommandReport>> =
        Result.success(tuple(onFailure(producer, publisher, config.language.container), this.asCommandReport()))

}

fun <A, B> NamedParser.asParseFailure(comment: String, guild: Guild, user: User, onFailure: (MessageProducer<A, B>, MessagePublisher<A, B>, LanguageContainer) -> IO<List<Order>>) =
    ParseFailure(this.name, comment, guild, user, onFailure)

fun ParseFailure<*, *>.asCommandReport() =
    CommandReport("PARSE-FAILURE-${this.name}", this.comment, this.guild, this.user)
