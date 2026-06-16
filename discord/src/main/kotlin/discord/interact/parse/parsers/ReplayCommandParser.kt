package discord.interact.parse.parsers

import core.database.entities.GameRecordId
import core.database.repositories.GameRecordRepository
import core.interact.commands.Command
import core.interact.commands.ReplayCommand
import discord.assets.messageRef
import discord.interact.UserInteractionContext
import discord.interact.parse.EmbeddableCommand
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent

object ReplayCommandParser : EmbeddableCommand {

    override suspend fun parseComponent(context: UserInteractionContext<GenericComponentInteractionCreateEvent>): Command? {
        val (_, recordId, _, validationKey) = when (context.event) {
            is StringSelectInteractionEvent -> context.event.interaction.selectedOptions.first().value
            else -> context.event.componentId
        }.split("-")

        if (validationKey != context.user.id.validationKey) {
            return null
        }

        val record = recordId
            .toLongOrNull()
            ?.let { GameRecordRepository.retrieveGameRecord(context.bot.dbConnection, GameRecordId(it)) }
            ?: return null

        return if (record.users.black.id == context.user.id || record.users.white.id == context.user.id)
            ReplayCommand(record, context.event.message.messageRef())
        else
            null
    }

}
