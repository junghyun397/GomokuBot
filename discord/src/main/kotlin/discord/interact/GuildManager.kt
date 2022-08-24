package discord.interact

import core.assets.MessageRef
import core.assets.aiUser
import core.assets.anonymousUser
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.session.ArchivePolicy
import core.session.GameResult
import core.session.entities.AiGameSession
import core.session.entities.GameSession
import core.session.entities.PvpGameSession
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.interactions.commands.updateCommands
import dev.minn.jda.ktx.messages.Message
import discord.assets.JDAGuild
import discord.interact.message.DiscordMessageProducer
import discord.interact.message.DiscordMessagePublisher
import discord.interact.message.MessageActionAdaptor
import discord.interact.parse.buildableCommands
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import utils.structs.Option
import utils.structs.flatMap
import utils.structs.map

object GuildManager {

    fun lookupPermission(channel: TextChannel, permission: Permission) =
        channel.guild.selfMember.hasPermission(channel, permission)

    suspend fun hasDebugPermission(config: DiscordConfig, user: User): Boolean =
        user.jda
            .getGuildById(config.officialServerId.idLong)!!
            .retrieveMemberById(user.idLong)
            .await()
            ?.roles
            ?.any { it.idLong == config.testerRoleId }
            ?: false

    inline fun <T> permissionGrantedRun(channel: TextChannel, permission: Permission, block: () -> T): Option<T> =
        if (this.lookupPermission(channel, permission)) Option(block())
        else Option.Empty

    inline fun <T> permissionNotGrantedRun(channel: TextChannel, permission: Permission, block: () -> T): Option<T> =
        if (this.lookupPermission(channel, permission)) Option.Empty
        else Option(block())

    fun initGlobalCommand(jda: JDA) {
        jda.updateCommands {
            slash(Language.ENG.container.helpCommand(), Language.ENG.container.helpCommandDescription())
        }.queue()
    }

    fun upsertCommands(guild: JDAGuild, container: LanguageContainer) {
        buildableCommands.fold(guild.updateCommands()) { action, command ->
            command.buildCommandData(action, container)
        }.queue()
    }

    private fun core.assets.User.switchToAnonymousUser() =
        when (this) {
            aiUser -> aiUser
            else -> anonymousUser
        }

    suspend fun archiveSession(archiveChannel: TextChannel, session: GameSession, archivePolicy: ArchivePolicy) {
        if (session.board.moves() < 20 || archivePolicy == ArchivePolicy.PRIVACY) return

        val modSession = when (archivePolicy) {
            ArchivePolicy.BY_ANONYMOUS -> when (session) {
                is AiGameSession -> session.copy(owner = anonymousUser)
                is PvpGameSession -> session.copy(owner = anonymousUser, opponent = anonymousUser)
            }
            else -> session
        }

        val modResult = when (archivePolicy) {
            ArchivePolicy.BY_ANONYMOUS -> modSession.gameResult.map { result ->
                when (result) {
                    is GameResult.Win -> result.copy(
                        winner = result.winner.switchToAnonymousUser(),
                        looser = result.looser.switchToAnonymousUser()
                    )
                    else -> result
                }
            }
            else -> modSession.gameResult
        }

        val publisher: DiscordMessagePublisher = { msg -> MessageActionAdaptor(archiveChannel.sendMessage(msg)) }

        DiscordMessageProducer.produceSessionArchive(publisher, modSession, modResult)
            .flatMap { it.launch() }
            .run()
    }

    suspend fun retrieveJDAMessage(jda: JDA, messageRef: MessageRef): net.dv8tion.jda.api.entities.Message? =
        jda.getGuildById(messageRef.guildId.idLong)
            ?.getTextChannelById(messageRef.channelId.idLong)
            ?.retrieveMessageById(messageRef.id.idLong)
            ?.await()

    fun retainFirstEmbed(message: net.dv8tion.jda.api.entities.Message) {
        message.editMessage(Message(embed = message.embeds.first()))
            .retainFiles(message.attachments) // TODO: JDA v10 API
            .queue()
    }

    fun removeComponents(message: net.dv8tion.jda.api.entities.Message) =
        message.editMessageComponents()
            .queue()

}
