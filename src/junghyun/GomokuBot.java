package junghyun;

import junghyun.ui.Message;
import junghyun.unit.Pos;
import junghyun.unit.Settings;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class GomokuBot {

    private static IDiscordClient client;

    public static void startGomokuBot() {
        System.out.println("Booting... GomokuBot, Start booting..");
        client = new ClientBuilder().withToken(Settings.TOKEN).build();
        client.getDispatcher().registerListener(new EventListener());
        client.login();
    }

    public static void endGomokuBot() {
        client.logout();
        System.out.println("Booting... Gomoku bot, End of execution.");
    }

    public static void processCommand(MessageReceivedEvent event) {
        String[] splitText = event.getMessage().getContent().toLowerCase().split(" ");

        switch (splitText[0]) {
            case "!help": //도움말
                Message.sendHelp(event.getAuthor(), event.getChannel());
                break;
            case "!rank": //순위
                Message.sendRank(event.getAuthor(), event.getChannel());
                break;
            case "!start": //시작
                GameManager.createGame(event.getAuthor().getLongID(), event.getAuthor(), event.getChannel());
                break;
            case "!gg": //포기
                GameManager.surrenGame(event.getAuthor().getLongID(), event.getAuthor(), event.getChannel());
                break;
            case "!s": //돌 놓기
                if (splitText.length != 3) break;
                if ((splitText[1].length() != 1) || (splitText[2].length() != 1)) break;

                Pos pos = new Pos(Pos.engToInt(splitText[1].toCharArray()[0]), Integer.getInteger(splitText[2]));

                GameManager.putStone(event.getAuthor().getLongID(), pos, event.getAuthor(), event.getChannel());
                break;
        }
    }

}
