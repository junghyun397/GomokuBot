typeshare native/mintaka --lang=kotlin --output-file=types.kt.raw

cat types.kt.header types.kt.raw > core/src/main/kotlin/core/engine/types/types.generated.kt
rm types.kt.raw
