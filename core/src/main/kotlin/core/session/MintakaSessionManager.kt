package core.session

import core.session.entities.MintakaIdleSession
import core.session.entities.MintakaLaunchingSession

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