package renju.native

import java.lang.foreign.Arena
import java.lang.foreign.SymbolLookup
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap

internal object NativeLibraryLoader {

    private val lookupArena: Arena = Arena.global()
    private val lookups = ConcurrentHashMap<String, SymbolLookup>()
    private val searchRoots = listOf(
        Paths.get("native/mintaka/target/release").toAbsolutePath().normalize(),
        Paths.get("native/mintaka/target/debug").toAbsolutePath().normalize(),
    )

    fun libraryLookup(name: String): SymbolLookup =
        lookups.computeIfAbsent(name) { libraryName ->
            val mappedFileName = System.mapLibraryName(libraryName)
            val libraryPath = searchRoots
                .firstNotNullOfOrNull { root ->
                    root.resolve(mappedFileName).takeIf(Files::isRegularFile)
                }
                ?: throw IllegalStateException(
                    "Native library '$libraryName' not found. Looked for '$mappedFileName' in: " +
                        searchRoots.joinToString(", ")
                )

            SymbolLookup.libraryLookup(libraryPath, lookupArena)
        }
}
