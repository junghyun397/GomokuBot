package core.session.entities

@JvmInline
value class MessageBufferKey(val key: String) {

    companion object {

        fun fromString(source: String): MessageBufferKey =
            MessageBufferKey(String(source.toCharArray() + System.currentTimeMillis().toString().toCharArray()))

    }

}
