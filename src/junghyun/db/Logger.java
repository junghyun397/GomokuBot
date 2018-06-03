package junghyun.db;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    private static String logBuffer = "\n";

    public static void startLogger() {
        System.out.println("Booting... Ready logger");
    }

    public static void loggerInfo(String text) {
        System.out.print("[" + getDateTime() + "] [INFO] " + text);
        logBuffer += text+"\n";
    }

    public static void loggerWarning(String text) {
        System.out.print("[" + getDateTime() + "] [Warning] " + text);
        logBuffer += text+"\n";
    }

    public static void saveLogs() {
        logBuffer = "\n";
    }

    private static String getDateTime() {
        long time = System.currentTimeMillis();
        SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        return dayTime.format(new Date(time));
    }

}
