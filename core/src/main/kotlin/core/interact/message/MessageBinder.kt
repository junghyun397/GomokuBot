package core.interact.message

import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import utils.monads.Either
import utils.monads.IO
import java.io.File

abstract class MessageBinder<A, B> {

    abstract fun bindBoard(publisher: MessagePublisher<A, B>, container: LanguageContainer, board: Either<String, File>): IO<Array<MessageAction<B>>>

    abstract fun bindButtons(boardAction: MessageAction<B>, container: LanguageContainer, commandMap: MiniBoardStatusMap): MessageAction<B>

    abstract fun bindAboutBot(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<B>>

    abstract fun bindCommandGuide(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<B>>

    abstract fun bindStyleGuide(publisher: MessagePublisher<A, B>, container: LanguageContainer): IO<MessageAction<B>>

    protected val languageEnumeration = Language.values()
        .fold(StringBuilder()) { builder, language ->
            builder.append(" ``${language.container.languageCode()}``")
        }
        .toString()

    abstract fun bindLanguageGuide(publisher: MessagePublisher<A, B>): IO<MessageAction<B>>

}
