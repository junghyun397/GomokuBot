package renju.native

import java.lang.foreign.*
import java.lang.foreign.MemoryLayout.PathElement.groupElement

internal class RustyRenjuImage internal constructor(
    lookup: SymbolLookup,
) {

    private val symbols = NativeSymbols(lookup, "rusty_renju_image")

    val constants: Constants = Constants(
        formatPng = symbols.byte("format_png"),
        formatWebp = symbols.byte("format_webp"),
        rendererNone = symbols.byte("renderer_none"),
        rendererLast = symbols.byte("renderer_last"),
        rendererPair = symbols.byte("renderer_pair"),
        rendererSequence = symbols.byte("renderer_sequence"),
    )

    private val imageRender = symbols.function(
        "render",
        BYTE_BUFFER_LAYOUT,
        ValueLayout.JAVA_BYTE,
        ValueLayout.JAVA_FLOAT,
        ValueLayout.JAVA_BYTE,
        ValueLayout.JAVA_BOOLEAN,
        ValueLayout.ADDRESS,
        ValueLayout.ADDRESS,
        ValueLayout.JAVA_LONG,
        ValueLayout.ADDRESS,
        ValueLayout.JAVA_LONG,
        ValueLayout.ADDRESS,
        ValueLayout.JAVA_LONG,
    )
    private val freeByteBuffer = symbols.voidFunction("free_byte_buffer", ValueLayout.ADDRESS)

    fun rusty_renju_image_render(
        imageFormat: Byte,
        webpQuality: Float,
        option: Byte,
        enableForbidden: Boolean,
        board: MemorySegment?,
        actions: IntArray?,
        actionsLen: Long,
        offers: IntArray?,
        offersLen: Long,
        blinds: IntArray?,
        blindsLen: Long,
    ): ByteBuffer {
        return Arena.ofConfined().use { arena ->
            val byteBufferSegment = imageRender.invokeWithArguments(
                arena,
                imageFormat,
                webpQuality,
                option,
                enableForbidden,
                board.orNullAddress(),
                actions.toNativeSegmentOrNull(arena),
                actionsLen,
                offers.toNativeSegmentOrNull(arena),
                offersLen,
                blinds.toNativeSegmentOrNull(arena),
                blindsLen,
            ) as MemorySegment

            ByteBuffer(
                ptr = byteBufferSegment.get(ValueLayout.ADDRESS, BYTE_BUFFER_PTR_OFFSET),
                len = byteBufferSegment.get(ValueLayout.JAVA_LONG, BYTE_BUFFER_LEN_OFFSET),
            )
        }
    }

    fun rusty_renju_image_free_byte_buffer(byteBuffer: ByteBuffer) {
        Arena.ofConfined().use { arena ->
            val bufferSegment = arena.allocate(BYTE_BUFFER_LAYOUT)
            bufferSegment.set(ValueLayout.ADDRESS, BYTE_BUFFER_PTR_OFFSET, byteBuffer.ptr)
            bufferSegment.set(ValueLayout.JAVA_LONG, BYTE_BUFFER_LEN_OFFSET, byteBuffer.len)

            freeByteBuffer.invokeWithArguments(bufferSegment)
        }
    }

    data class Constants(
        val formatPng: Byte,
        val formatWebp: Byte,
        val rendererNone: Byte,
        val rendererLast: Byte,
        val rendererPair: Byte,
        val rendererSequence: Byte,
    )

    data class ByteBuffer(val ptr: MemorySegment, val len: Long)

    private companion object {

        private val BYTE_BUFFER_LAYOUT = MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withName("ptr"),
            ValueLayout.JAVA_LONG.withName("len"),
        )

        private val BYTE_BUFFER_PTR_OFFSET = BYTE_BUFFER_LAYOUT.byteOffset(groupElement("ptr"))
        private val BYTE_BUFFER_LEN_OFFSET = BYTE_BUFFER_LAYOUT.byteOffset(groupElement("len"))

    }

}

internal object RustyRenjuImageApi {

    val lib: RustyRenjuImage by lazy { RustyRenjuImage(NativeLibraryLoader.libraryLookup("rusty_renju_image")) }

    val constants: RustyRenjuImage.Constants get() = lib.constants

}
