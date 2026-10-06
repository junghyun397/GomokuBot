package renju.native

import java.lang.foreign.Arena
import java.lang.foreign.SymbolLookup
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

internal object NativeLibraryLoader {

    private val searchRoot = this.findSearchRoot()

    fun libraryLookup(name: String): SymbolLookup {
        val mappedFileName = System.mapLibraryName(name)
        val libraryPath = this.searchRoot.resolve(mappedFileName).takeIf(Files::isRegularFile)
            ?: throw IllegalStateException("Native library '$mappedFileName' not found in ${this.searchRoot}")

        return SymbolLookup.libraryLookup(libraryPath, Arena.global())
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
