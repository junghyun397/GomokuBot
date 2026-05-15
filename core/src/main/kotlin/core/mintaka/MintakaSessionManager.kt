package core.mintaka

object MintakaSessionManager {

    fun createMintakaSession(): MintakaIdleSession {
        throw NotImplementedError()
    }

    fun playMintakaSession(): MintakaLaunchingSession {
        throw NotImplementedError()
    }

    fun abortMintakaSession(): MintakaIdleSession {
        throw NotImplementedError()
    }

}
