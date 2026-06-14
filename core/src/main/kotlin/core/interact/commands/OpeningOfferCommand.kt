package core.interact.commands

import core.assets.MessageRef
import core.session.entities.GameSession
import core.session.entities.OfferStageOpeningSession
import core.session.entities.SessionId
import renju.notation.Pos

class OpeningOfferCommand(
    sessionId: SessionId,
    move: Pos,
    responseFlag: ResponseFlag,
    messageRef: MessageRef?,
) : OpeningMoveCommand<OfferStageOpeningSession>(sessionId, move, responseFlag, messageRef) {

    override val name = "opening-offer"

    override fun selectSession(session: GameSession) = session as? OfferStageOpeningSession

    override fun executeSelf(session: OfferStageOpeningSession) = session.add(this.move)

    override fun writeLog() = "add offer ${this.move}"

}
