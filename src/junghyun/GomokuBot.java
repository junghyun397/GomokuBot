package junghyun;

import junghyun.discord.BotManager;
import junghyun.discord.db.DBManager;
import junghyun.discord.db.Logger;
import junghyun.discord.db.SqlManager;

import javax.security.auth.login.LoginException;
import java.util.Scanner;

public class GomokuBot {

    private static Scanner scanner;

    private static boolean onRunning = false;

    private static Logger logger;
    private static BotManager botManager;

    public static void main(String[] args) {
        GomokuBot.startServer();
    }

    private static void startServer() {
        Logger logger = new Logger();

        SqlManager sqlManager = new SqlManager(logger);
        DBManager dbManager = new DBManager(logger, sqlManager);

        BotManager botManager = new BotManager(logger, sqlManager, dbManager);

        GomokuBot.logger = logger;
        GomokuBot.botManager = botManager;

        logger.startLogger();
        sqlManager.connectMysql();

        try {
            botManager.startBotManager();
            GomokuBot.onRunning = true;
            logger.loggerInfo("booting succeed!");
            GomokuBot.startScanCommand();
        } catch (LoginException | InterruptedException e) {
            logger.loggerWarning(e.getMessage());
        }
    }

    private static void stopServer() {
        GomokuBot.botManager.endGomokuBot();

        GomokuBot.logger.saveLogs();
        GomokuBot.onRunning = false;
    }

    private static void startScanCommand() {
        GomokuBot.scanner = new Scanner(System.in);
        GomokuBot.scanCommand();
    }

    private static void scanCommand() {
        while (GomokuBot.onRunning) {
            String command = GomokuBot.scanner.nextLine();
            switch (command) {
                case "stop":
                    GomokuBot.stopServer();
                    break;
                case "restart":
                    GomokuBot.stopServer();
                    GomokuBot.startServer();
                case "save":
                    GomokuBot.logger.saveLogs();
                case "status":
                    GomokuBot.logger.loggerDev("game count : " + GomokuBot.botManager.getGameManager().getGameListSize());
                    GomokuBot.logger.loggerDev("server count : " + GomokuBot.botManager.getClient().getGuilds().size());
                    break;
//              case "broadcast-all":
//                  String text = command.split("broadcast-all")[0];
//                  for (Guild guild: BotManager.getClient().getGuilds())
//                      Objects.requireNonNull(guild.getSystemChannel()).sendMessage(text).complete();
//                  Logger.loggerDev("send Broadcast : " + text);
//                  break;
            }
        }
    }

}