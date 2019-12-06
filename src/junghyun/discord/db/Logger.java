package junghyun.discord.db;

import junghyun.discord.Settings;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Logger {

    private static StringBuffer logBuffer = new StringBuffer("\n");
    private static StringBuffer commandBuffer = new StringBuffer("\n");

    public static void startLogger() {
        Runnable task = Logger::saveLogs;

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(task, Settings.LOGGER_SAVE, Settings.LOGGER_SAVE, TimeUnit.SECONDS);
    }

    public static void loggerDev(String text) {
        text = "[ ] " + getDateTime() + text + "\n";
        System.out.print(text);
    }

    public static void loggerInfo(String text) {
        text = "[+] " + getDateTime() + text + "\n";
        System.out.print(text);
        Logger.logBuffer.append(text);
    }

    public static void loggerWarning(String text) {
        text = "[!] " + getDateTime() + text + "\n";
        System.out.print(text);
        Logger.logBuffer.append(text);
    }

    public static void loggerCommand(String text) {
        Logger.commandBuffer.append(getDateTime()).append(" COMMAND: ").append(text).append("\n");
    }

    public static void saveLogs() {
        try {
            Logger.writeLog("log.txt", Logger.logBuffer);
            Logger.writeLog("log-cmd.txt", Logger.commandBuffer);

            Logger.loggerInfo("Log saved!");
        } catch (Exception e) {
            Logger.loggerWarning(e.getMessage());
        }
    }

    private static String getDateTime() {
        long time = System.currentTimeMillis();
        SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss >> ");
        return dayTime.format(new Date(time));
    }

    private static void writeLog(String fd, StringBuffer buffer) throws IOException {
        BufferedWriter fwLog = new BufferedWriter(new FileWriter(fd, true));
        fwLog.write(buffer.toString());
        fwLog.flush();
        fwLog.close();
        buffer.setLength(0);
    }

}
