typeshare native/mintaka --lang=kotlin --output-file=mintaka.kt.raw

cat mintaka.kt.header.raw mintaka.kt.raw > core/src/main/kotlin/core/mintaka/mintaka.kt
