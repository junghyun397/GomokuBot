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

    public static void saveLogs() {
        try {
            BufferedWriter fw = new BufferedWriter(new FileWriter("log.txt", true));
            fw.write(Logger.logBuffer);
            fw.flush();
            fw.close();
            Logger.logBuffer = "";
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
