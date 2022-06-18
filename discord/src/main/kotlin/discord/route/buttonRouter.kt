@file:Suppress("DuplicatedCode")

package discord.route

import core.interact.reports.CommandReport
import dev.minn.jda.ktx.await
import discord.assets.extractUser
import discord.interact.InteractionContext
import discord.interact.message.DiscordMessageAdaptor
import discord.interact.message.DiscordMessageProducer
import discord.interact.message.WebHookActionAdaptor
import discord.interact.parse.EmbeddableCommand
import discord.interact.parse.parsers.AcceptCommandParser
import discord.interact.parse.parsers.ConfigCommandParser
import discord.interact.parse.parsers.RejectCommandParser
import discord.interact.parse.parsers.SetCommandParser
import kotlinx.coroutines.async
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple2
import utils.lang.component1
import utils.lang.component2
import utils.structs.Option
import utils.structs.asOption

private fun matchAction(prefix: String): Option<EmbeddableCommand> =
    when (prefix) {
        "s" -> Option(SetCommandParser)
        "a" -> Option(AcceptCommandParser)
        "r" -> Option(RejectCommandParser)
        "p" -> Option(ConfigCommandParser)
        else -> Option.Empty
    }

fun buttonInteractionRouter(context: InteractionContext<GenericComponentInteractionCreateEvent>): Mono<Tuple2<InteractionContext<GenericComponentInteractionCreateEvent>, Result<CommandReport>>> =
    Mono.zip(
        context.toMono(),
        context.event.component.id.asOption().flatMap {
            matchAction(
                prefix = it.split("-").first()
            )
        }.toMono()
    )
        .flatMap { (context, parsable) -> Mono.zip(
            context.toMono(),
            mono { parsable.flatMap { parsable ->
                parsable.parseButton(context)
            } }
        ) }
        .filter { (_, parsable) -> parsable.isDefined }
        .doOnNext { (context, _) ->
            context.event
                .deferReply().queue()
        }
        .flatMap { (context, command) -> Mono.zip(context.toMono(), mono { command.getOrException()
            .execute(
                bot = context.bot,
                config = context.config,
                user = context.event.user.extractUser(),
                message = async { DiscordMessageAdaptor(context.event.hook.retrieveOriginal().await()) },
                producer = DiscordMessageProducer,
                publisher = { msg -> WebHookActionAdaptor(context.event.hook.sendMessage(msg)) }
            )
        }) }
        .flatMap { (context, result) -> Mono.zip(context.toMono(), mono { result.map { (io, report) ->
            export(context, io, context.event.message)
            report
        } }) }
