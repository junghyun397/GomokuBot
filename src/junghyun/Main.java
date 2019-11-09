package junghyun;

import junghyun.discord.BotManager;
import junghyun.discord.GameManager;
import junghyun.discord.db.Logger;
import net.dv8tion.jda.api.entities.Guild;

import javax.security.auth.login.LoginException;
import java.util.Objects;
import java.util.Scanner;

public class Main {

    private static Scanner scanner;

    private static boolean onRunning = false;

    public static void main(String[] args) throws LoginException, InterruptedException {
        Logger.startLogger();
        BotManager.startGomokuBot();
        Main.onRunning = true;
        Logger.loggerInfo("booting succeed!");
        Main.startServerCommand();
    }

    private static void stopServer() {
        BotManager.endGomokuBot();
        Logger.saveLogs();
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
                case "save-log":
                    Logger.saveLogs();
                case "count-game":
                    Logger.loggerDev("game count : " + GameManager.getGameListSize());
                    break;
                case "count-server":
                    Logger.loggerDev("server count : " + BotManager.getClient().getGuilds().size());
                    break;
                case "broadcast-all":
                    String text = command.split("broadcast-all")[0];
                    for (Guild guild: BotManager.getClient().getGuilds())
                        Objects.requireNonNull(guild.getSystemChannel()).sendMessage(text).complete();
                    Logger.loggerDev("send Broadcast : " + text);
                    break;
            }
        }
    }

}