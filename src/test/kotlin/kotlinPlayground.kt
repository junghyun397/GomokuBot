import java.util.*

class TestClass(private val data: Int) : Comparable<TestClass> {

    override operator fun compareTo(other: TestClass) = data.compareTo(other.data)

    override fun toString(): String = "$data"

}

fun main() {
    val tree: TreeSet<TestClass> = sortedSetOf(TestClass(9), TestClass(5), TestClass(7))
    tree.add(TestClass(8))
    tree.remove(TestClass(5))
    println(tree)
}
