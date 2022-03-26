package core.interact.message

import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import utils.monads.IO

abstract class MessageBinder<A, B> {

    abstract fun bindAboutBot(container: LanguageContainer, publisher: MessagePublisher<A, B>): IO<Unit>

    abstract fun bindCommandGuide(container: LanguageContainer, publisher: MessagePublisher<A, B>): IO<Unit>

    abstract fun bindStyleGuide(container: LanguageContainer, publisher: MessagePublisher<A, B>): IO<Unit>

    protected val languageEnumeration = Language.values()
        .fold(StringBuilder()) { builder, language ->
            builder.append(" ``${language.container.languageCode()}``")
        }
        .toString()

    abstract fun bindLanguageGuide(publisher: MessagePublisher<A, B>): IO<Unit>

}
