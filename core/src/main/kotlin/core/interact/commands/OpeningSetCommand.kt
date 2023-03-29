package core.interact.commands

import core.assets.MessageRef
import core.session.entities.MoveStageOpeningSession
import renju.notation.Pos

// TODO: WTF?
class OpeningSetCommand(
    session: MoveStageOpeningSession,
    move: Pos,
    deployAt: MessageRef?,
    responseFlag: ResponseFlag
) : OpeningMoveCommand<MoveStageOpeningSession>(session, move, deployAt, responseFlag) {

    override val name = "opening-set"

    override fun executeSelf() = this.session.next(move)

    override fun writeLog() = "make move $move"

}
