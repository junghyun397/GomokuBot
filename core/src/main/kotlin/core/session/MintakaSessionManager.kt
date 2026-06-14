package core.session

import core.engine.MintakaIdleSession
import core.engine.MintakaLaunchingSession

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