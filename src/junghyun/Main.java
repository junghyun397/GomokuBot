package junghyun;

import junghyun.discord.BotManager;
import junghyun.discord.db.DBManager;
import junghyun.discord.db.Logger;
import junghyun.discord.db.SqlManager;

import javax.security.auth.login.LoginException;
import java.util.Scanner;

public class Main {

    private static Scanner scanner;

    private static boolean onRunning = false;

    private static Logger logger;
    private static BotManager botManager;

    public static void main(String[] args) {
        Main.startServer();
    }

    private static void startServer() {
        Logger logger = new Logger();

        SqlManager sqlManager = new SqlManager(logger);
        DBManager dbManager = new DBManager(logger, sqlManager);

        BotManager botManager = new BotManager(logger, sqlManager, dbManager);

        Main.logger = logger;
        Main.botManager = botManager;

        logger.startLogger();
        sqlManager.connectMysql();

        try {
            botManager.startBotManager();
            Main.onRunning = true;
            logger.loggerInfo("booting succeed!");
            Main.startServerCommand();
        } catch (LoginException | InterruptedException e) {
            logger.loggerWarning(e.getMessage());
        }
    }

    private static void stopServer() {
        Main.botManager.endGomokuBot();

        Main.logger.saveLogs();
        Main.onRunning = false;
    }

    private static void startServerCommand() {
        Main.scanner = new Scanner(System.in);
        Main.scanCommand();
    }

    private static void scanCommand() {
        while (Main.onRunning) {
            String command = Main.scanner.nextLine();
            switch (command) {
                case "stop":
                    Main.stopServer();
                    break;
                case "restart":
                    Main.stopServer();
                    Main.startServer();
                case "save":
                    Main.logger.saveLogs();
                case "status":
                    Main.logger.loggerDev("game count : " + Main.botManager.getGameManager().getGameListSize());
                    Main.logger.loggerDev("server count : " + Main.botManager.getClient().getGuilds().size());
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