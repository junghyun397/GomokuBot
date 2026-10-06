package core.interact.message

import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import utils.MarkdownAnchorMapping
import utils.SimplifiedMarkdownDocument
import utils.parseSimplifiedMarkdownDocument

object HelpPages {

    val documents: Map<LanguageContainer, Pair<SimplifiedMarkdownDocument, MarkdownAnchorMapping>> =
        Language.entries.associate { language ->
            language.container to parseSimplifiedMarkdownDocument(language.container.aboutRenjuDocument)
        }

}
