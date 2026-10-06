package renju.native

import java.lang.foreign.*

internal fun IntArray?.toNativeSegmentOrNull(arena: Arena): MemorySegment =
    this?.takeUnless(IntArray::isEmpty)?.let { arena.allocateFrom(ValueLayout.JAVA_INT, *it) } ?: MemorySegment.NULL

internal fun nativeStruct(vararg fields: MemoryLayout): StructLayout {
    val members = mutableListOf<MemoryLayout>()
    var size = 0L

    fun pad(alignment: Long) {
        val padding = (alignment - size % alignment) % alignment

        if (padding > 0) {
            members += MemoryLayout.paddingLayout(padding)
            size += padding
        }
    }

    for (field in fields) {
        pad(field.byteAlignment())
        members += field
        size += field.byteSize()
    }

    pad(fields.maxOf { it.byteAlignment() })

    return MemoryLayout.structLayout(*members.toTypedArray())
}
