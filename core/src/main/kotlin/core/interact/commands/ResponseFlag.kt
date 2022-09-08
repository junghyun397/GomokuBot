@file:Suppress("unused")

package core.interact.commands

sealed interface ResponseFlag {

    object Immediately : ResponseFlag

    data class Defer(val edit: Boolean = false) : ResponseFlag

    companion object {

        val Defer = Defer()

        val DeferEdit = Defer(true)

    }

}
