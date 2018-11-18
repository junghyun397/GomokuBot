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
    }

    private static void stopServer() {
        GomokuBot.endGomokuBot();
        Logger.saveLogs();
        onRunning = false;
    }

    private static void startServerCommand() {
        scanner = new Scanner(System.in);
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