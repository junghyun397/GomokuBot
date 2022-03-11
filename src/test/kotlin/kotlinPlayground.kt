interface I

data class A<T> (val v: T)

class B <T: I> (val v: T)

class C(val v: String) : I

inline fun <reified T : I> function(data: Pair<A<String>, B<T>>) = "${data.first.v}, ${data.second.v}"

fun main() {
}