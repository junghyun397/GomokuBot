package discord.interact.message

import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.interact.message.MessageAction
import core.interact.message.SentMessage
import discord.assets.awaitNullable
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.requests.FluentRestAction
import net.dv8tion.jda.api.utils.messages.MessageCreateRequest
import net.dv8tion.jda.api.utils.messages.MessageEditRequest

typealias DiscordMessageAction = MessageAction

class MessageCreateAdaptor<T>(private val original: T) : DiscordMessageAction
        where T : MessageCreateRequest<T>, T : FluentRestAction<Message, T> {

    override fun launch(): Effect<Nothing, Unit> = effect {
        this@MessageCreateAdaptor.original.queue({}, {})
    }

    override fun retrieve(): Effect<Nothing, SentMessage?> = effect {
        this@MessageCreateAdaptor.original.awaitNullable()?.let { DiscordSentMessage(it) }
    }

}

class WebHookMessageCreateAdaptor<T>(private val original: T) : DiscordMessageAction
        where T : MessageCreateRequest<T>, T : FluentRestAction<InteractionHook, T> {

    override fun launch(): Effect<Nothing, Unit> = effect {
        this@WebHookMessageCreateAdaptor.original.queue({}, {})
    }

    override fun retrieve(): Effect<Nothing, SentMessage?> = effect {
        this@WebHookMessageCreateAdaptor.original.awaitNullable()?.retrieveOriginal()?.awaitNullable()?.let { DiscordSentMessage(it) }
    }

}

class MessageEditAdaptor<T>(private val original: T) : DiscordMessageAction
        where T : MessageEditRequest<T>, T : FluentRestAction<Message, T> {

    override fun launch(): Effect<Nothing, Unit> = effect {
        this@MessageEditAdaptor.original.queue({}, {})
    }

    override fun retrieve(): Effect<Nothing, SentMessage?> = effect {
        this@MessageEditAdaptor.original.awaitNullable()?.let { DiscordSentMessage(it) }
    }

}

class WebHookMessageEditAdaptor<T>(private val original: T) : DiscordMessageAction
        where T : MessageEditRequest<T>, T : FluentRestAction<InteractionHook, T> {

    override fun launch(): Effect<Nothing, Unit> = effect {
        this@WebHookMessageEditAdaptor.original.queue({}, {})
    }

    override fun retrieve(): Effect<Nothing, SentMessage?> = effect {
        this@WebHookMessageEditAdaptor.original.awaitNullable()?.retrieveOriginal()?.awaitNullable()?.let { DiscordSentMessage(it) }
    }

}
