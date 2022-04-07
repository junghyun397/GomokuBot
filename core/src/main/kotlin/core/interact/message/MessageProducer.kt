package core.interact.message

import core.assets.User
import core.database.entities.SimpleProfile
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.interact.message.graphics.BoardRenderer
import core.session.entities.GameSession
import jrenju.notation.Pos
import utils.structs.IO

abstract class MessageProducer<A, B> {

    // BOARD

    abstract fun produceBoard(publisher: MessagePublisher<A, B>, container: LanguageContainer, renderer: BoardRenderer, session: GameSession): IO<MessageAction<B>>

    abstract fun attachButtons(boardAction: MessageAction<B>, container: LanguageContainer, focused: FocusedFields): MessageAction<B>

    // GAME

    abstract fun produceBeginsPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageAction<B>>

    abstract fun produceBeginsPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User): IO<MessageAction<B>>

    abstract fun produceNextMove(publisher: MessagePublisher<A, B>, container: LanguageContainer, previousPlayer: User, nextPlayer: User, latestMove: Pos): IO<MessageAction<B>>

    abstract fun produceWinPVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, winner: User, looser: User, latestMove: Pos): IO<MessageAction<B>>

    abstract fun produceTiePVP(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageAction<B>>

    abstract fun produceWinPVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, latestMove: Pos): IO<MessageAction<B>>

    abstract fun produceLosePVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, latestMove: Pos): IO<MessageAction<B>>

    abstract fun produceTiePVE(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User): IO<MessageAction<B>>

    abstract fun produceSurrendered(publisher: MessagePublisher<A, B>, container: LanguageContainer, winner: User, looser: User): IO<MessageAction<B>>

    // HELP

    abstract fun produceAboutBot(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<B>>

    abstract fun produceCommandGuide(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<B>>

    // RANK

    abstract fun produceRankings(publisher: MessagePublisher<A, B>, container: LanguageContainer, rankings: Set<SimpleProfile>): IO<MessageAction<B>>

    // RATING

    abstract fun produceRating(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<B>>

    // LANG

    protected val languageEnumeration = Language.values()
        .fold(StringBuilder()) { builder, language ->
            builder.append(" ``${language.container.languageCode()}``")
        }
        .toString()

    abstract fun produceLanguageGuide(publisher: MessagePublisher<A, B>): IO<MessageAction<B>>

    abstract fun produceLanguageNotFound(publisher: MessagePublisher<A, B>): IO<MessageAction<B>>

    abstract fun produceLanguageUpdated(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<B>>

    // STYLE

    abstract fun produceStyleGuide(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<B>>

    abstract fun produceStyleNotFound(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<B>>

    abstract fun produceStyleUpdated(publisher: MessagePublisher<A, B>, container: LanguageContainer, style: String): IO<MessageAction<B>>

    // POLICY

    // SESSION

    abstract fun produceSessionNotFound(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<B>>

    // START

    abstract fun produceSessionAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, opponent: User): IO<MessageAction<B>>

    abstract fun produceRequestAlready(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User): IO<MessageAction<B>>

    // SET

    abstract fun produceSetIllegalArgument(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<B>>

    abstract fun produceSetAlreadyExist(publisher: MessagePublisher<A, B>, container: LanguageContainer, user: User, pos: Pos): IO<MessageAction<B>>

    abstract fun produceSetForbiddenMove(publisher: MessagePublisher<A, B>, container: LanguageContainer, user: User, pos: Pos, forbiddenFlag: Byte): IO<MessageAction<B>>

    // REQUEST

    abstract fun produceRequest(publisher: MessagePublisher<A, B>, container: LanguageContainer, owner: User, opponent: User): IO<MessageAction<B>>

    // UTILS

    abstract fun produceNotYetImplemented(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<B>>

}
