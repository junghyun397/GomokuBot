package renju.native

import com.sun.jna.NativeLibrary
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicBoolean

internal object NativeLibraryLoader {

    private val initialized = AtomicBoolean(false)
    private val nativeLibraryPaths = listOf(
        Paths.get("native/mintaka/target/release").toAbsolutePath().normalize().toString(),
        Paths.get("native/mintaka/target/debug").toAbsolutePath().normalize().toString(),
    )

    fun ensureLoaded() {
        if (!initialized.compareAndSet(false, true)) {
            return
        }

        for (name in listOf("rusty_renju_c", "rusty_renju_image")) {
            for (path in nativeLibraryPaths) {
                NativeLibrary.addSearchPath(name, path)
            }
        }
    }
}
