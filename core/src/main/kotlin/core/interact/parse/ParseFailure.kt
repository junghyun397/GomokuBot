package core.interact.parse

import arrow.core.raise.Effect
import core.assets.Channel
import core.assets.User
import core.interact.commands.CommandResult
import core.interact.i18n.LanguageContainer
import core.interact.message.MessagePublisher
import core.interact.message.PlatformService
import core.interact.reports.CommandActionLog
import core.session.entities.ChannelConfig
import kotlin.time.Clock
import kotlin.time.Instant

class ParseFailure(
    val name: String,
    val comment: String,
    val channel: Channel,
    val user: User.Human,
    private val onFailure: (PlatformService, MessagePublisher, LanguageContainer) -> Effect<Nothing, Unit>
) {

    fun notice(
        config: ChannelConfig,
        service: PlatformService,
        publisher: MessagePublisher,
        emittedTime: Instant
    ): Result<CommandResult> =
        Result.success(CommandResult(onFailure(service, publisher, config.language.container), this.asActionLog(emittedTime)))

}

fun CommandParser.asParseFailure(comment: String, channel: Channel, user: User.Human, onFailure: (PlatformService, MessagePublisher, LanguageContainer) -> Effect<Nothing, Unit>) =
    ParseFailure(this.name, comment, channel, user, onFailure)

fun ParseFailure.asActionLog(emittedTime: Instant) =
    CommandActionLog(
        "PARSE-FAILURE-${this.name}",
        this.channel,
        this.user,
        this.comment,
        Clock.System.now() - emittedTime
    )
