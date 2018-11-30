package junghyun;

import junghyun.unit.Settings;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;

public class EventListener {

    @EventSubscriber
    public void onMessageReceivedEvent(MessageReceivedEvent event) {
        if ((event.getMessage().getType() != IMessage.Type.DEFAULT)
                || (event.getMessage().getContent().length() < 2)
                || (event.getMessage().getContent().toCharArray()[0] != Settings.PREFIX)) return;
        GomokuBot.processCommand(event);
    }

}
