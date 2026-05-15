package renju.native

import java.lang.foreign.*
import java.lang.invoke.MethodHandle

internal class NativeSymbols(
    private val lookup: SymbolLookup,
    private val prefix: String,
) {

    private val linker = Linker.nativeLinker()

    fun byte(name: String): Byte = function(name, ValueLayout.JAVA_BYTE).invokeWithArguments() as Byte

    fun int(name: String): Int = function(name, ValueLayout.JAVA_INT).invokeWithArguments() as Int

    fun function(name: String, result: MemoryLayout, vararg arguments: MemoryLayout): MethodHandle =
        linker.downcallHandle(symbol(name), FunctionDescriptor.of(result, *arguments))

    fun voidFunction(name: String, vararg arguments: MemoryLayout): MethodHandle =
        linker.downcallHandle(symbol(name), FunctionDescriptor.ofVoid(*arguments))

    private fun symbol(name: String): MemorySegment {
        val symbolName = "${prefix}_$name"
        return lookup.find(symbolName)
            .orElseThrow { IllegalStateException("Native symbol '$symbolName' not found") }
    }

}

internal fun ByteArray?.toNativeSegmentOrNull(arena: Arena): MemorySegment =
    this?.takeUnless(ByteArray::isEmpty)?.let { arena.allocateFrom(ValueLayout.JAVA_BYTE, *it) } ?: MemorySegment.NULL

internal fun MemorySegment?.orNullAddress(): MemorySegment = this ?: MemorySegment.NULL

internal fun MemorySegment.nullIfNull(): MemorySegment? = if (this == MemorySegment.NULL) null else this
