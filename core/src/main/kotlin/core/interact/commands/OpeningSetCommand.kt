package core.interact.commands

import core.assets.MessageRef
import core.session.entities.GameSession
import core.session.entities.MoveStageOpeningSession
import core.session.entities.SessionId
import renju.notation.Pos

class OpeningSetCommand(
    sessionId: SessionId,
    move: Pos,
    responseFlag: ResponseFlag,
    messageRef: MessageRef?,
) : OpeningMoveCommand<MoveStageOpeningSession>(sessionId, move, responseFlag, messageRef) {

    override val name = "opening-set"

    override fun selectSession(session: GameSession) = session as? MoveStageOpeningSession

    override fun executeSelf(session: MoveStageOpeningSession) = session.next(this.move)

    override fun writeLog() = "make move ${this.move}"

}
