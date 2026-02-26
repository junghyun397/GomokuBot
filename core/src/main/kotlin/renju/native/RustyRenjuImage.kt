package renju.native

import java.lang.foreign.*
import java.lang.foreign.MemoryLayout.PathElement.groupElement
import java.lang.invoke.MethodHandle

internal class RustyRenjuImage internal constructor(
    lookup: SymbolLookup,
) {

    private val linker = Linker.nativeLinker()

    private val rustyRenjuImageFormatPng = downcall(lookup, "rusty_renju_image_format_png", FunctionDescriptor.of(ValueLayout.JAVA_BYTE))
    private val rustyRenjuImageFormatWebp = downcall(lookup, "rusty_renju_image_format_webp", FunctionDescriptor.of(ValueLayout.JAVA_BYTE))

    private val rustyRenjuImageRendererNone = downcall(lookup, "rusty_renju_image_renderer_none", FunctionDescriptor.of(ValueLayout.JAVA_BYTE))
    private val rustyRenjuImageRendererLast = downcall(lookup, "rusty_renju_image_renderer_last", FunctionDescriptor.of(ValueLayout.JAVA_BYTE))
    private val rustyRenjuImageRendererPair = downcall(lookup, "rusty_renju_image_renderer_pair", FunctionDescriptor.of(ValueLayout.JAVA_BYTE))
    private val rustyRenjuImageRendererSequence = downcall(lookup, "rusty_renju_image_renderer_sequence", FunctionDescriptor.of(ValueLayout.JAVA_BYTE))

    private val rustyRenjuImageRender = downcall(
        lookup,
        "rusty_renju_image_render",
        FunctionDescriptor.of(
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
        ),
    )

    private val rustyRenjuImageFreeByteBuffer = downcall(
        lookup,
        "rusty_renju_image_free_byte_buffer",
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS),
    )

    fun rusty_renju_image_format_png(): Byte = rustyRenjuImageFormatPng.callByte()

    fun rusty_renju_image_format_webp(): Byte = rustyRenjuImageFormatWebp.callByte()

    fun rusty_renju_image_renderer_none(): Byte = rustyRenjuImageRendererNone.callByte()

    fun rusty_renju_image_renderer_last(): Byte = rustyRenjuImageRendererLast.callByte()

    fun rusty_renju_image_renderer_pair(): Byte = rustyRenjuImageRendererPair.callByte()

    fun rusty_renju_image_renderer_sequence(): Byte = rustyRenjuImageRendererSequence.callByte()

    fun rusty_renju_image_render(
        imageFormat: Byte,
        webpQuality: Float,
        option: Byte,
        enableForbidden: Boolean,
        board: MemorySegment?,
        actions: ByteArray?,
        actionsLen: Long,
        offers: ByteArray?,
        offersLen: Long,
        blinds: ByteArray?,
        blindsLen: Long,
    ): ByteBuffer {
        return Arena.ofConfined().use { arena ->
            val actionsSegment = actions.toNativeSegmentOrNull(arena)
            val offersSegment = offers.toNativeSegmentOrNull(arena)
            val blindsSegment = blinds.toNativeSegmentOrNull(arena)

            val byteBufferSegment = rustyRenjuImageRender.callStruct(
                imageFormat,
                webpQuality,
                option,
                enableForbidden,
                board.orNullAddress(),
                actionsSegment,
                actionsLen,
                offersSegment,
                offersLen,
                blindsSegment,
                blindsLen,
            )

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

            rustyRenjuImageFreeByteBuffer.callVoid(bufferSegment)
        }
    }

    data class ByteBuffer(
        val ptr: MemorySegment,
        val len: Long,
    )

    private fun downcall(
        lookup: SymbolLookup,
        symbolName: String,
        descriptor: FunctionDescriptor,
    ): MethodHandle {
        val symbol = lookup.find(symbolName)
            .orElseThrow { IllegalStateException("Native symbol '$symbolName' not found") }

        return linker.downcallHandle(symbol, descriptor)
    }

    private fun ByteArray?.toNativeSegmentOrNull(arena: Arena): MemorySegment {
        val data = this ?: return MemorySegment.NULL
        if (data.isEmpty()) {
            return MemorySegment.NULL
        }

        val segment = arena.allocate(data.size.toLong())
        for (idx in data.indices) {
            segment.set(ValueLayout.JAVA_BYTE, idx.toLong(), data[idx])
        }

        return segment
    }

    private fun MemorySegment?.orNullAddress(): MemorySegment = this ?: MemorySegment.NULL

    private fun MethodHandle.callByte(vararg args: Any): Byte =
        invokeWithArguments(*args) as Byte

    private fun MethodHandle.callStruct(vararg args: Any): MemorySegment =
        invokeWithArguments(*args) as MemorySegment

    private fun MethodHandle.callVoid(vararg args: Any) {
        invokeWithArguments(*args)
    }

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

    val lib: RustyRenjuImage by lazy {
        RustyRenjuImage(NativeLibraryLoader.libraryLookup("rusty_renju_image"))
    }

    val constants: Constants by lazy {
        Constants(
            formatPng = lib.rusty_renju_image_format_png(),
            formatWebp = lib.rusty_renju_image_format_webp(),
            rendererNone = lib.rusty_renju_image_renderer_none(),
            rendererLast = lib.rusty_renju_image_renderer_last(),
            rendererPair = lib.rusty_renju_image_renderer_pair(),
            rendererSequence = lib.rusty_renju_image_renderer_sequence(),
        )
    }

    data class Constants(
        val formatPng: Byte,
        val formatWebp: Byte,
        val rendererNone: Byte,
        val rendererLast: Byte,
        val rendererPair: Byte,
        val rendererSequence: Byte,
    )

}
