package interact.message.graphics

import interact.i18n.LanguageContainer
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.components.ActionRow

class ImageBoardRenderer : BoardRenderer {

    override fun buildEmbeds(languageContainer: LanguageContainer): Array<MessageEmbed> {
        TODO("Not yet implemented")
    }

    override fun buildButtons(): Array<ActionRow> {
        TODO("Not yet implemented")
    }

    companion object : BoardRendererSample {

        override val styleShortcut = "A"

        override val styleName = "IMAGE"

        override val sampleView = ""

    }

}
