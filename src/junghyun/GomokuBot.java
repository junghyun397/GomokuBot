package junghyun;

import junghyun.db.DBManager;
import junghyun.db.SqlManager;
import junghyun.ui.Message;
import junghyun.unit.Pos;
import junghyun.unit.Settings;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;

import java.util.Objects;

class GomokuBot {

    private static IDiscordClient client;

    static void startGomokuBot() {
        System.out.println("Booting... GomokuBot, Start booting..");
        client = new ClientBuilder().setPresence(StatusType.ONLINE, ActivityType.PLAYING, "GomokuBot.github.io or ~help").withToken(Settings.TOKEN).build();
        client.getDispatcher().registerListener(new EventListener());
        client.login();

        SqlManager.connectMysql();
        GameManager.bootGameManager();
    }

    static void endGomokuBot() {
        GomokuBot.client.logout();
        System.out.println("Booting... Gomoku bot, End of execution.");
    }

    static void processCommand(MessageReceivedEvent event) {
        String[] splitText = event.getMessage().getContent().toLowerCase().split(" ");

        switch (splitText[0]) {
            case "~help":
                Message.sendHelp(event.getChannel());
                break;
            case "~rank":
                Message.sendRank(event.getAuthor(), event.getChannel(), Objects.requireNonNull(DBManager.getRankingData(Settings.RANK_COUNT)));
                break;
            case "~start":
                GameManager.createGame(event.getAuthor().getLongID(), event.getAuthor(), event.getChannel());
                break;
            case "~resign":
                GameManager.resignGame(event.getAuthor().getLongID(), event.getAuthor(), event.getChannel());
                break;
            case "~s":
                if ((splitText.length != 3) || (!((splitText[1].length() == 1) && ((splitText[2].length() == 1) || (splitText[2].length() == 2))))) {
                    Message.sendErrorGrammarSet(event.getAuthor(), event.getChannel());
                    break;
                }

                Pos pos = new Pos(Pos.engToInt(splitText[1].toLowerCase().toCharArray()[0]), Integer.valueOf(splitText[2].toLowerCase())-1);
                if (!Pos.checkSize(pos.getX(), pos.getY())) {
                    Message.sendErrorGrammarSet(event.getAuthor(), event.getChannel());
                    break;
                }

                GameManager.putStone(event.getAuthor().getLongID(), pos, event.getAuthor(), event.getChannel());
                break;
        }
    }

}
