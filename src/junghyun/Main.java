package junghyun;

import junghyun.db.Logger;

import java.util.Scanner;

public class Main {

    private static Scanner scanner;

    private static boolean onRunning = false;

    public static void main(String[] args) {
        GomokuBot.startGomokuBot();
        Logger.startLogger();
        onRunning = true;
        startServerCommand();
        Logger.loggerInfo("Start working now!");
    }

    private static void stopServer() {
        GomokuBot.endGomokuBot();
        Logger.saveLogs();
        onRunning = false;
        Logger.loggerInfo("It is finished now!");
    }

    private static void startServerCommand() {
        scanner = new Scanner(System.in);
        System.out.println("Booting... Ready for listen command");
        scanCommand();
    }

    private static void scanCommand() {
        if (!onRunning) return;

        String command = scanner.nextLine();
        switch (command) {
            case "stop":
                stopServer();
                break;
            case "count":
                break;
        }
        scanCommand();
    }

}