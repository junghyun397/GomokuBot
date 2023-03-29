package core.interact.commands

import core.assets.MessageRef
import core.session.entities.OfferStageOpeningSession
import renju.notation.Pos

class OpeningOfferCommand(
    session: OfferStageOpeningSession,
    move: Pos,
    deployAt: MessageRef?,
    responseFlag: ResponseFlag
) : OpeningMoveCommand<OfferStageOpeningSession>(session, move, deployAt, responseFlag) {

    override val name = "opening-offer"

    override fun executeSelf() = this.session.add(move)

    override fun writeLog() = "add offer $move"

}
