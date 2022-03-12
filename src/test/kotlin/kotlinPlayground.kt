import kotlin.Exception

interface I

data class A<T> (val v: T)

class B <T: I> (val v: T)

class C(val v: String) : I

inline fun <reified T : I> function(data: Pair<A<String>, B<T>>) = "${data.first.v}, ${data.second.v}"

@Suppress("UNREACHABLE_CODE")
fun main() {
    val exception: Result<Unit> = runCatching { throw Exception("eRRoR") }
    exception.onFailure {
        println(it.stackTraceToString())
    }
}