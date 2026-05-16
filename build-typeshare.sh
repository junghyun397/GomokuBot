typeshare native/mintaka --lang=kotlin --output-file=types.kt.raw

cat types.kt.header types.kt.raw > core/src/main/kotlin/core/mintaka/types/types.generated.kt
