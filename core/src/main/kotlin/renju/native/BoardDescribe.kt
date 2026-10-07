package renju.native

import renju.notation.*
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemoryLayout.PathElement.groupElement
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout.*

internal class BoardDescribe(segment: MemorySegment) {

    val hashKey = HashKey(segment.get(JAVA_LONG, hashKeyOffset))
    val playerColor = checkNotNull(Color.from(segment.get(JAVA_BYTE, playerColorOffset)))

    val fivePos = ColorContainer(
        black = this.readFivePos(segment, fivePosOffset),
        white = this.readFivePos(segment, fivePosOffset + fivePosLayout.byteSize()),
    )

    val cells = Array(Pos.BOARD_SIZE) { index ->
        val offset = fieldOffset + index * cellLayout.byteSize()
        val kind = segment.get(JAVA_BYTE, offset + cellKindOffset)
        val content = segment.get(JAVA_BYTE, offset + cellContentOffset)

        Cell(
            stone = if (kind == RustyRenju.exportItemStone) Color.from(content) else null,
            forbidden = if (kind == RustyRenju.exportItemForbidden) ForbiddenKind.from(content) else null,
        )
    }

    val winner: Winner? =
        if (segment.get(JAVA_BYTE, winnerPresentOffset) == 0.toByte()) {
            null
        } else {
            Winner(
                color = checkNotNull(Color.from(segment.get(JAVA_BYTE, winnerColorOffset))),
                sequence = List(5) { index ->
                    Pos.fromIdx(segment.get(JAVA_INT, winnerSequenceOffset + index * JAVA_INT.byteSize()))
                },
            )
        }

    private fun readFivePos(segment: MemorySegment, offset: Long): List<Pos?> =
        List(2) { index ->
            Pos.fromIdxOrNone(segment.get(JAVA_BYTE, offset + index).toInt() and 0xFF)
        }

    data class Cell(val stone: Color?, val forbidden: ForbiddenKind?)

    data class Winner(val color: Color, val sequence: List<Pos>)

    companion object {

        private val fivePosLayout = MemoryLayout.sequenceLayout(2, JAVA_BYTE)

        private val cellLayout = nativeStruct(
            JAVA_BYTE.withName("kind"),
            JAVA_BYTE.withName("content"),
        )
        private val winnerLayout = nativeStruct(
            JAVA_BYTE.withName("is_some"),
            JAVA_BYTE.withName("color"),
            MemoryLayout.sequenceLayout(5, JAVA_INT).withName("sequence"),
        )

        val layout = nativeStruct(
            JAVA_LONG.withName("hash_key"),
            JAVA_BYTE.withName("player_color"),
            MemoryLayout.sequenceLayout(2, MemoryLayout.sequenceLayout(4, JAVA_LONG)).withName("bitfield"),
            MemoryLayout.sequenceLayout(2, this.fivePosLayout).withName("five_pos"),
            MemoryLayout.sequenceLayout(Pos.BOARD_SIZE.toLong(), this.cellLayout).withName("field"),
            this.winnerLayout.withName("winner"),
        )

        private val cellKindOffset = this.cellLayout.byteOffset(groupElement("kind"))
        private val cellContentOffset = this.cellLayout.byteOffset(groupElement("content"))
        private val hashKeyOffset = this.layout.byteOffset(groupElement("hash_key"))
        private val playerColorOffset = this.layout.byteOffset(groupElement("player_color"))
        private val fivePosOffset = this.layout.byteOffset(groupElement("five_pos"))
        private val fieldOffset = this.layout.byteOffset(groupElement("field"))
        private val winnerPresentOffset = this.layout.byteOffset(groupElement("winner"), groupElement("is_some"))
        private val winnerColorOffset = this.layout.byteOffset(groupElement("winner"), groupElement("color"))
        private val winnerSequenceOffset = this.layout.byteOffset(groupElement("winner"), groupElement("sequence"))

    }

}
