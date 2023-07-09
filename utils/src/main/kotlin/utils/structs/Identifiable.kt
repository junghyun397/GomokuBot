package utils.structs

import kotlin.enums.EnumEntries

interface Identifiable {

    val id: Short

}

fun <T> EnumEntries<T>.find(id: Short): T where T : Enum<T>, T : Identifiable =
    find { it.id == id }!!
