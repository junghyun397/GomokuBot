package junghyun;

import junghyun.unit.Settings;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class EventListener {

    @EventSubscriber
    public void handle(ReadyEvent readyEvent) {
    }

    @EventSubscriber
    public void onMessageReceivedEvent(MessageReceivedEvent event) {
        if ((event.getMessage().getContent().toCharArray()[0] != Settings.PREFIX) && (event.getMessage().getContent().length() < 2)) return;
        GomokuBot.processCommand(event);
    }

}
