package core.interact.message

import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import utils.structs.Either
import utils.structs.IO
import java.io.File

abstract class MessageProducer<A, B> {

    // EMBEDS

    abstract fun produceBoard(publisher: MessagePublisher<A, B>, container: LanguageContainer, board: Either<String, File>): IO<Array<MessageAction<B>>>

    abstract fun attachButtons(boardAction: MessageAction<B>, container: LanguageContainer, focused: FocusedFields): MessageAction<B>

    abstract fun produceAboutBot(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<B>>

    abstract fun produceCommandGuide(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<B>>

    abstract fun produceStyleGuide(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<B>>

    protected val languageEnumeration = Language.values()
        .fold(StringBuilder()) { builder, language ->
            builder.append(" ``${language.container.languageCode()}``")
        }
        .toString()

    abstract fun produceLanguageGuide(publisher: MessagePublisher<A, B>): IO<MessageAction<B>>

    // ## LANG

    // ## STYLE

    // ## RANK

    // ## RATING

    // ## START

    // ## RESIGN

    // ## UTILS

    abstract fun produceNotYetImplemented(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<B>>

}
