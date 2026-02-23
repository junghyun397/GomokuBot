package renju.native

import com.sun.jna.*

internal interface RustyRenjuImage : Library {

    fun rusty_renju_image_format_png(): Byte

    fun rusty_renju_image_format_webp(): Byte

    fun rusty_renju_image_renderer_none(): Byte

    fun rusty_renju_image_renderer_last(): Byte

    fun rusty_renju_image_renderer_pair(): Byte

    fun rusty_renju_image_renderer_sequence(): Byte

    fun rusty_renju_image_render(
        imageFormat: Byte,
        webpQuality: Float,
        option: Byte,
        enableForbidden: Boolean,
        board: Pointer?,
        actions: ByteArray?,
        actionsLen: Long,
        offers: ByteArray?,
        offersLen: Long,
        blinds: ByteArray?,
        blindsLen: Long,
    ): ByteBuffer.ByValue

    fun rusty_renju_image_free_byte_buffer(byteBuffer: ByteBuffer)

    @Structure.FieldOrder("ptr", "len")
    open class ByteBuffer : Structure {

        @JvmField
        var ptr: Pointer? = null

        @JvmField
        var len: NativeLong = NativeLong(0)

        constructor() : super()

        constructor(pointer: Pointer?) : super(pointer) {
            read()
        }

        class ByValue : ByteBuffer(), Structure.ByValue

    }

}

internal object RustyRenjuImageApi {

    val lib: RustyRenjuImage by lazy {
        NativeLibraryLoader.ensureLoaded()
        Native.load("rusty_renju_image", RustyRenjuImage::class.java)
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
