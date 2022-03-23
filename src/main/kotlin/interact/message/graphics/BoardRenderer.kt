package interact.message.graphics

import interact.i18n.LanguageContainer
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.components.ActionRow

enum class BoardStyle(val renderer: BoardRenderer, val sample: BoardRendererSample) {
    IMAGE(ImageBoardRenderer(), ImageBoardRenderer),
    TEXT(TextBoardRenderer(), TextBoardRenderer),
    SOLID_TEXT(SolidTextBoardRenderer(), SolidTextBoardRenderer),
    UNICODE(UnicodeBoardRenderer(), UnicodeBoardRenderer)
}

sealed interface BoardRendererSample {

    val styleShortcut: String

    val styleName: String

    val sampleView: String

}

sealed interface BoardRenderer {

    fun buildEmbeds(languageContainer: LanguageContainer): Array<MessageEmbed>

    fun buildButtons(): Array<ActionRow>

}
