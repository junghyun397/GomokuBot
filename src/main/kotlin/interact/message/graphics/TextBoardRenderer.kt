package interact.message.graphics

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.Message
import interact.i18n.LanguageContainer
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import utility.COLOR_GREEN_HEX
import utility.EMOJI_BLACK_CIRCLE
import utility.EMOJI_WHITE_CIRCLE
import utility.MessagePublisher

open class TextBoardRenderer : BoardRenderer {

    override fun buildEmbeds(languageContainer: LanguageContainer) = emptyArray<MessageEmbed>()

    override fun buildButtons() = emptyArray<ActionRow>()

    fun attachBoardWithButtons(messagePublisher: MessagePublisher) =
        messagePublisher(Message(
            embeds = listOf(
                Embed {
                    color = COLOR_GREEN_HEX
                    description = ":mag: 버튼을 누르거나 ``/s`` ``알파벳`` ``숫자`` 명령어를 입력해 다음 수를 놓아 주세요."
                }
            )
        )).fold(
            onDefined = { it.addActionRow(
                ActionRow.of(
                    Button.of(ButtonStyle.SECONDARY, "e1", "e1"),
                    Button.of(ButtonStyle.SECONDARY, "e2", "", EMOJI_BLACK_CIRCLE).asDisabled(),
                    Button.of(ButtonStyle.SECONDARY, "e3", "", EMOJI_WHITE_CIRCLE).asDisabled(),
                    Button.of(ButtonStyle.SECONDARY, "e4", "e4"),
                    Button.of(ButtonStyle.SECONDARY, "e5", "e5"),
                ),
                ActionRow.of(
                    Button.of(ButtonStyle.SECONDARY, "d1", "d1"),
                    Button.of(ButtonStyle.SECONDARY, "d2", "", EMOJI_WHITE_CIRCLE).asDisabled(),
                    Button.of(ButtonStyle.SECONDARY, "d3", "", EMOJI_WHITE_CIRCLE).asDisabled(),
                    Button.of(ButtonStyle.SECONDARY, "d4", "", EMOJI_BLACK_CIRCLE).asDisabled(),
                    Button.of(ButtonStyle.SECONDARY, "d5", "d5"),
                ),
                ActionRow.of(
                    Button.of(ButtonStyle.SECONDARY, "c1", "c1"),
                    Button.of(ButtonStyle.SECONDARY, "c2", "c2"),
                    Button.of(ButtonStyle.SECONDARY, "c3", "", EMOJI_BLACK_CIRCLE).asDisabled(),
                    Button.of(ButtonStyle.SECONDARY, "c4", "c4"),
                    Button.of(ButtonStyle.SECONDARY, "c5", "c5"),
                ),
                ActionRow.of(
                    Button.of(ButtonStyle.SECONDARY, "b1", "b1"),
                    Button.of(ButtonStyle.SECONDARY, "b2", "b2"),
                    Button.of(ButtonStyle.SECONDARY, "b3", "b3"),
                    Button.of(ButtonStyle.SECONDARY, "b4", "b4"),
                    Button.of(ButtonStyle.SECONDARY, "b5", "b5"),
                ),
                ActionRow.of(
                    Button.of(ButtonStyle.SECONDARY, "a1", "a1"),
                    Button.of(ButtonStyle.SECONDARY, "a2", "a2"),
                    Button.of(ButtonStyle.SECONDARY, "a3", "a3"),
                    Button.of(ButtonStyle.SECONDARY, "a4", "a4"),
                    Button.of(ButtonStyle.SECONDARY, "a5", "a5"),
                )
            ).queue() },
            onEmpty = { }
        )


    companion object : BoardRendererSample {

        override val styleShortcut = "B"

        override val styleName = "TEXT"

        override val sampleView = "```\n" +
                "  A B C D\n" +
                "4     O   4\n" +
                "3   X   X 3\n" +
                "2   O X   2\n" +
                "1 O   O   1\n" +
                "  A B C D\n" +
                "```"
    }

}
