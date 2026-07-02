package core.interact.reports

import core.assets.Channel
import core.assets.User
import core.interact.commands.Command
import core.interact.commands.InternalCommand
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

sealed interface ActionLog {

    val action: String

    val commandExecutionTime: Duration

    fun buildBody(): String = this.action

}

data class CommandActionLog(
    val commandName: String,
    val channel: Channel,
    val user: User.Human?,
    override val action: String,
    override val commandExecutionTime: Duration
) : ActionLog {

    override fun buildBody(): String =
        this.user
            ?.let { user -> "${this.channel}/${user}\t ${this.commandName}\t ${this.action}" }
            ?: "${this.channel}\t ${this.commandName}\t ${this.action}"

}

data class ErrorActionLog(
    val error: Throwable,
    override val commandExecutionTime: Duration
) : ActionLog {

    override val action: String = "error"

    override fun buildBody(): String = "${this.action}\t ${this.error.stackTraceToString()}"

}

data class RoutineActionLog(
    override val action: String,
    override val commandExecutionTime: Duration
) : ActionLog

fun Command.writeActionLog(emittedTime: Instant, action: String, channel: Channel, user: User.Human) =
    CommandActionLog(this.name, channel, user, action, Clock.System.now() - emittedTime)

fun InternalCommand.writeActionLog(emittedTime: Instant, action: String, channel: Channel) =
    CommandActionLog(this.name, channel, null, action, Clock.System.now() - emittedTime)
