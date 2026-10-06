package discord.interact.parse.parsers

import core.BotContext
import core.assets.COLOR_NORMAL_HEX
import core.assets.MessageRef
import core.interact.commands.Command
import core.session.NavigationManager
import core.session.entities.NavigationState
import core.session.entities.PageNavigationState
import discord.assets.awaitNullable
import discord.assets.messageRef
import discord.interact.UserInteractionContext
import discord.interact.message.DiscordComponentIds
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent

object ReactionCommandParser {

    suspend fun parseReaction(context: UserInteractionContext<GenericMessageReactionEvent>): Command? {
        val messageRef = context.event.messageRef()
        val cachedState = NavigationManager.getNavigationState(context.bot.sessions, messageRef)
        if (cachedState != null)
            return NavigationCommandParser.parseReaction(context, cachedState)

        val message = context.event.retrieveMessage().awaitNullable() ?: return null
        if (message.author.idLong != context.event.jda.selfUser.idLong) return null

        return if (this.isBoardMessage(message)) {
            FocusCommandParser.parseReaction(context)
        } else {
            this.recoverNavigationState(context.bot, message, messageRef)
                ?.let { NavigationCommandParser.parseReaction(context, it) }
        }
    }

    private fun isBoardMessage(message: Message): Boolean =
        message.componentTree.findAll(Button::class.java).any { button ->
            button.customId?.startsWith("${DiscordComponentIds.SET}-") == true ||
                button.customId?.startsWith("${DiscordComponentIds.OPENING}-") == true
        }

    private fun recoverNavigationState(bot: BotContext, message: Message, messageRef: MessageRef): NavigationState? =
        message.embeds.firstOrNull()
            ?.let { PageNavigationState.decodeFromColor(COLOR_NORMAL_HEX, it.colorRaw, bot.dbConnection) }
            ?.also { NavigationManager.addNavigation(bot.sessions, messageRef, it) }

}
