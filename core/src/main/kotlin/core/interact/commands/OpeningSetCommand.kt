package core.interact.commands

import core.assets.MessageRef
import core.session.entities.GameSession
import core.session.entities.MoveStageOpeningSession
import core.session.entities.SessionId
import renju.notation.Pos

class OpeningSetCommand(
    sessionId: SessionId,
    move: Pos,
    deployAt: MessageRef?,
    responseFlag: ResponseFlag
) : OpeningMoveCommand<MoveStageOpeningSession>(sessionId, move, deployAt, responseFlag) {

    override val name = "opening-set"

    override fun selectSession(session: GameSession) = session as? MoveStageOpeningSession

    override fun executeSelf(session: MoveStageOpeningSession) = session.next(this.move)

    override fun writeLog() = "make move ${this.move}"

}
