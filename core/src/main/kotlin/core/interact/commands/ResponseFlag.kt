@file:Suppress("unused")

package core.interact.commands

sealed interface ResponseFlag {

    data object Immediately : ResponseFlag

    data class Defer(
        val edit: Boolean = false,
        val windowed: Boolean = false
    ) : ResponseFlag

    companion object {

        val Defer = Defer()

        val DeferWindowed = Defer(windowed = true)

        val DeferEdit = Defer(edit = true)

    }

}
