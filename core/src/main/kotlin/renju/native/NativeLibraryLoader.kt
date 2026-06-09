package renju.native

import java.lang.foreign.Arena
import java.lang.foreign.SymbolLookup
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap

internal object NativeLibraryLoader {

    private val lookupArena: Arena = Arena.global()
    private val lookups = ConcurrentHashMap<String, SymbolLookup>()
    private val searchRoot = findSearchRoot()

    fun libraryLookup(name: String): SymbolLookup =
        this.lookups.computeIfAbsent(name) { libraryName ->
            val mappedFileName = System.mapLibraryName(libraryName)
            val libraryPath = this.searchRoot.resolve(mappedFileName).takeIf(Files::isRegularFile)
                ?: throw IllegalStateException("Native library '$mappedFileName' not found in ${this.searchRoot}")

            SymbolLookup.libraryLookup(libraryPath, this.lookupArena)
        }

    private fun findSearchRoot(): Path {
        val relative = Paths.get("native/mintaka/target/release")
        var path = Paths.get("").toAbsolutePath().normalize()

        while (true) {
            val candidate = path.resolve(relative)

            if (Files.isDirectory(candidate)) {
                return candidate
            }

            path = path.parent ?: break
        }

        return relative.toAbsolutePath().normalize()
    }
}
