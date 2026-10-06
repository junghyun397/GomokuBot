package renju.native

import java.lang.foreign.*
import java.lang.invoke.MethodHandle

internal class NativeSymbols(
    private val lookup: SymbolLookup,
    private val prefix: String,
) {

    private val linker = Linker.nativeLinker()

    fun byte(name: String) = this.function(name, ValueLayout.JAVA_BYTE).invokeWithArguments() as Byte

    fun int(name: String) = this.function(name, ValueLayout.JAVA_INT).invokeWithArguments() as Int

    fun long(name: String) = this.function(name, ValueLayout.JAVA_LONG).invokeWithArguments() as Long

    fun function(name: String, result: MemoryLayout, vararg arguments: MemoryLayout): MethodHandle =
        this.linker.downcallHandle(this.symbol(name), FunctionDescriptor.of(result, *arguments))

    fun voidFunction(name: String, vararg arguments: MemoryLayout): MethodHandle =
        this.linker.downcallHandle(this.symbol(name), FunctionDescriptor.ofVoid(*arguments))

    private fun symbol(name: String): MemorySegment {
        val symbolName = "${this.prefix}_$name"
        return this.lookup.find(symbolName)
            .orElseThrow { IllegalStateException("Native symbol '$symbolName' not found") }
    }

}
