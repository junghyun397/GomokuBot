package junghyun.db;

import junghyun.unit.Settings;

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

        System.out.println("Booting... Ready logger");
    }

    public static void loggerInfo(String text) {
        System.out.print("[" + getDateTime() + "] [INFO] " + text);
        Logger.logBuffer += text+"\n";
    }

    public static void loggerWarning(String text) {
        System.out.print("[" + getDateTime() + "] [WARNING] " + text);
        Logger.logBuffer += text+"\n";
    }

    public static void saveLogs() {
        try {
            FileWriter fw = new FileWriter(Logger.getDateTime() + ".txt");
            BufferedWriter bw = new BufferedWriter(fw);
            String str = logBuffer;
            bw.write(str);
            bw.newLine();
            bw.close();
            Logger.logBuffer = "\n";
        } catch (Exception e) {
            Logger.loggerWarning(e.getMessage());
        }
    }

    private static String getDateTime() {
        long time = System.currentTimeMillis();
        SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        return dayTime.format(new Date(time));
    }

}
