package junghyun.discord.db;

import junghyun.discord.Settings;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Logger {

    private static String logBuffer = "\n";
    private static String commandBuffer = "\n";

    public static void startLogger() {
        Runnable task = Logger::saveLogs;

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(task, Settings.LOGGER_SAVE, Settings.LOGGER_SAVE, TimeUnit.SECONDS);
    }

    public static void loggerDev(String text) {
        text = "[" + getDateTime() + "] [Dev] " + text + "\n";
        System.out.print(text);
    }

    public static void loggerInfo(String text) {
        text = "[" + getDateTime() + "] [INFO] " + text + "\n";
        System.out.print(text);
        Logger.logBuffer += text;
    }

    public static void loggerWarning(String text) {
        text = "[" + getDateTime() + "] [WARNING] " + text + "\n";
        System.out.print(text);
        Logger.logBuffer += text;
    }

    public static void loggerCommand(String text) {
        text = "[" + getDateTime() + "] [COMMAND] " + text + "\n";
        Logger.commandBuffer += text;
    }

    public static void saveLogs() {
        try {
            BufferedWriter fwLog = new BufferedWriter(new FileWriter("log.txt", true));
            fwLog.write(Logger.logBuffer);
            fwLog.flush();
            fwLog.close();
            Logger.logBuffer = "";

            BufferedWriter fwCmd = new BufferedWriter(new FileWriter("log_cmd.txt", true));
            fwCmd.write(Logger.commandBuffer);
            fwCmd.flush();
            fwCmd.close();
            Logger.commandBuffer = "";

            Logger.loggerInfo("Log saved!");
        } catch (Exception e) {
            Logger.loggerWarning(e.getMessage());
        }
    }

    private static String getDateTime() {
        long time = System.currentTimeMillis();
        SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
        return dayTime.format(new Date(time));
    }

}
