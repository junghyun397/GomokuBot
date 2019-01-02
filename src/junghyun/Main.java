package junghyun;

import junghyun.discord.BotManager;
import junghyun.discord.GameManager;
import junghyun.discord.db.Logger;
import sx.blah.discord.handle.obj.IGuild;

import java.util.Scanner;

public class Main {

    private static Scanner scanner;

    private static boolean onRunning = false;

    public static void main(String[] args) {
        Logger.startLogger();
        BotManager.startGomokuBot();
        Main.onRunning = true;
        Logger.loggerInfo("----------------------------------");
        Logger.loggerInfo("Boot Done!");
        Logger.loggerInfo("----------------------------------");
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
                case "save_log":
                    Logger.saveLogs();
                case "game_count":
                    Logger.loggerDev("Game count : " + GameManager.getGameListSize());
                    break;
                case "server_count":
                    Logger.loggerDev("Server count : " + BotManager.getClient().getGuilds().size());
                    break;
                case "broadcast_all":
                    String text = command.split("broadcast_all")[0];
                    for (IGuild guild: BotManager.getClient().getGuilds()) guild.getSystemChannel().sendMessage(text);
                    Logger.loggerDev("Send Broadcast : " + text);
                    break;
            }
        }
    }

}