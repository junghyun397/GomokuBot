package renju.native

import java.lang.foreign.Arena
import java.lang.foreign.SymbolLookup
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap

internal object NativeLibraryLoader {

    private val lookupArena: Arena = Arena.global()
    private val lookups = ConcurrentHashMap<String, SymbolLookup>()
    private val searchRoot = Paths.get("native/mintaka/target/release").toAbsolutePath().normalize()

    fun libraryLookup(name: String): SymbolLookup =
        lookups.computeIfAbsent(name) { libraryName ->
            val mappedFileName = System.mapLibraryName(libraryName)
            val libraryPath = searchRoot.resolve(mappedFileName).takeIf(Files::isRegularFile)
                ?: throw IllegalStateException()

            SymbolLookup.libraryLookup(libraryPath, lookupArena)
        }
}
