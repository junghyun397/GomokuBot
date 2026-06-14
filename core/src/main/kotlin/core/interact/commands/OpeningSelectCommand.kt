package core.interact.commands

import core.assets.MessageRef
import core.session.entities.GameSession
import core.session.entities.SelectStageOpeningSession
import core.session.entities.SessionId
import renju.notation.Pos

class OpeningSelectCommand(
    sessionId: SessionId,
    move: Pos,
    responseFlag: ResponseFlag,
    messageRef: MessageRef?,
) : OpeningMoveCommand<SelectStageOpeningSession>(sessionId, move, responseFlag, messageRef) {

    override val name = "opening-select"

    override fun selectSession(session: GameSession) = session as? SelectStageOpeningSession

    override fun executeSelf(session: SelectStageOpeningSession) = session.select(this.move)

    override fun writeLog() = "select offer ${this.move}"

}
