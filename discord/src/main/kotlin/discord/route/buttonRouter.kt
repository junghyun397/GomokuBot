@file:Suppress("DuplicatedCode")

package discord.route

import core.interact.reports.CommandReport
import discord.interact.InteractionContext
import discord.interact.message.DiscordMessageAdaptor
import discord.interact.message.DiscordMessageProducer
import discord.interact.message.WebHookActionAdaptor
import discord.interact.message.WebHookUpdateActionAdaptor
import discord.interact.parse.EmbeddableCommand
import discord.interact.parse.parsers.AcceptCommandParser
import discord.interact.parse.parsers.ApplySettingCommandParser
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
import utils.structs.flatMap
import utils.structs.getOrException

private fun matchAction(prefix: String): Option<EmbeddableCommand> =
    when (prefix) {
        "s" -> Option(SetCommandParser)
        "a" -> Option(AcceptCommandParser)
        "r" -> Option(RejectCommandParser)
        "p" -> Option(ApplySettingCommandParser)
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
        .doOnNext { (context, _) -> context.event.deferReply().queue() }
        .flatMap { (context, command) -> Mono.zip(context.toMono(), mono { command.getOrException()
            .execute(
                bot = context.bot,
                config = context.config,
                guild = context.guild,
                user = context.user,
                producer = DiscordMessageProducer,
                message = async { DiscordMessageAdaptor(context.event.message) },
                publisher = { msg -> WebHookActionAdaptor(context.event.hook.sendMessage(msg)) },
                editPublisher = { msg -> WebHookUpdateActionAdaptor(context.event.hook.editOriginal(msg)) },
            )
        }) }
        .flatMap { (context, result) -> Mono.zip(context.toMono(), mono { result.map { (io, report) ->
            export(context, io, context.event.message)
            report
        } }) }
