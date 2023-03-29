package core.interact.commands

import core.assets.MessageRef
import core.session.entities.SelectStageOpeningSession
import renju.notation.Pos

class OpeningSelectCommand(
    session: SelectStageOpeningSession,
    move: Pos,
    deployAt: MessageRef?,
    responseFlag: ResponseFlag
) : OpeningMoveCommand<SelectStageOpeningSession>(session, move, deployAt, responseFlag) {

    override val name = "opening-select"

    override fun executeSelf() = this.session.select(move)

    override fun writeLog() = "select offer $move"

}
