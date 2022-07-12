package utils.structs

interface Identifiable {

    val id: Short

}

fun <T : Identifiable> Array<T>.find(id: Short) =
    find { it.id == id }!!
