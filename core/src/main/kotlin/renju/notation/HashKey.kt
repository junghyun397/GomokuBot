package renju.notation

@JvmInline value class HashKey(val value: Long) {

    override fun toString(): String =
        "0x${java.lang.Long.toHexString(this.value).padStart(HEX_DIGITS, '0')}"

    companion object {

        fun from(source: String): HashKey? =
            runCatching {
                val hex = source.lowercase()
                    .removePrefix("0x")

                HashKey(java.lang.Long.parseUnsignedLong(hex, 16))
            }.getOrNull()

        private const val HEX_DIGITS = 16

    }

}
