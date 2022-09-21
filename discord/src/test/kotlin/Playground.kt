import core.BotConfig
import core.assets.COLOR_NORMAL_HEX
import core.assets.VOID_MESSAGE_REF
import core.session.entities.NavigationKind
import core.session.entities.PageNavigationState
import discord.interact.message.DiscordMessageProducer
import okhttp3.internal.toHexString
import org.junit.Test
import utils.assets.LinuxTime
import utils.structs.getOrException

class Playground {

    @Test
    fun divide() {
        val a = 345

        val b = a / 2

        val c = a shr 1
    }

    @Test
    fun bitTwiddling() {
        val ps = PageNavigationState(VOID_MESSAGE_REF, NavigationKind.ABOUT, 12, LinuxTime.now())

        val ec = DiscordMessageProducer.encodePagenavigationState(COLOR_NORMAL_HEX, ps)

        val dc = DiscordMessageProducer.decodePagenavigationState(COLOR_NORMAL_HEX, ec, BotConfig(), VOID_MESSAGE_REF).getOrException()

        println(ps)
        println(ec.toHexString())
        println(dc)
    }

}
