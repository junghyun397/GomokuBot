package renju.native

import java.lang.foreign.Arena
import java.lang.foreign.MemoryLayout.PathElement.groupElement
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout.*

internal object RustyRenjuImage {

    private val symbols = NativeSymbols(NativeLibraryLoader.libraryLookup("rusty_renju_image"), "rusty_renju_image")

    val rendererLast = this.symbols.byte("renderer_last")
    val rendererPair = this.symbols.byte("renderer_pair")
    val rendererSequence = this.symbols.byte("renderer_sequence")

    private val formatPng = this.symbols.byte("format_png")
    private val bufferLayout = nativeStruct(ADDRESS.withName("ptr"), JAVA_LONG.withName("len"))
    private val pointerOffset = this.bufferLayout.byteOffset(groupElement("ptr"))
    private val sizeOffset = this.bufferLayout.byteOffset(groupElement("len"))

    private val renderCall = this.symbols.function(
        "render", this.bufferLayout,
        JAVA_BYTE, JAVA_FLOAT, JAVA_BYTE, JAVA_BOOLEAN,
        ADDRESS,
        ADDRESS, JAVA_LONG,
        ADDRESS, JAVA_LONG,
        ADDRESS, JAVA_LONG,
    )
    private val freeBufferCall = this.symbols.voidFunction("free_byte_buffer", ADDRESS)

    fun renderPng(
        board: MemorySegment,
        actions: IntArray?,
        option: Byte,
        enableForbidden: Boolean,
        offers: IntArray?,
        blinds: IntArray?,
    ): ByteArray =
        Arena.ofConfined().use { arena ->
            val buffer = this.renderCall.invokeWithArguments(
                arena,
                this.formatPng,
                1.0f,
                option,
                enableForbidden,
                board,
                actions.toNativeSegmentOrNull(arena),
                actions?.size?.toLong() ?: 0L,
                offers.toNativeSegmentOrNull(arena),
                offers?.size?.toLong() ?: 0L,
                blinds.toNativeSegmentOrNull(arena),
                blinds?.size?.toLong() ?: 0L,
            ) as MemorySegment

            try {
                val pointer = buffer.get(ADDRESS, this.pointerOffset)
                val size = buffer.get(JAVA_LONG, this.sizeOffset)

                check(pointer != MemorySegment.NULL) { "Native renderer returned null pointer" }
                check(size in 1L..Int.MAX_VALUE.toLong()) { "Native renderer returned invalid payload size: $size" }

                pointer.reinterpret(size).toArray(JAVA_BYTE)
            } finally {
                this.freeBufferCall.invokeWithArguments(buffer)
            }
        }

}
