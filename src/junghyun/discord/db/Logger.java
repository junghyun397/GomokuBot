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

    private final StringBuffer logBuffer = new StringBuffer("\n");
    private final StringBuffer commandBuffer = new StringBuffer("\n");

    public void startLogger() {
        Runnable task = this::saveLogs;

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(task, Settings.LOGGER_SAVE, Settings.LOGGER_SAVE, TimeUnit.SECONDS);
    }

    public void loggerDev(String text) {
        System.out.print("[ ] " + getDateTime() + text + "\n");
    }

    public void loggerInfo(String text) {
        final StringBuilder loggerBuffer = new StringBuilder();
        loggerBuffer.append("[+] ").append(getDateTime()).append(text).append("\n");

        System.out.print(loggerBuffer);
        this.logBuffer.append(loggerBuffer);

        loggerBuffer.setLength(0);
    }

    public void loggerWarning(String text) {
        final StringBuilder loggerBuffer = new StringBuilder();
        loggerBuffer.append("[!] ").append(getDateTime()).append(text).append("\n");

        System.out.print(loggerBuffer);
        this.logBuffer.append(loggerBuffer);

        loggerBuffer.setLength(0);
    }

    public void loggerCommand(String text) {
        this.commandBuffer.append(getDateTime()).append(" COMMAND: ").append(text).append("\n");
    }

    public void saveLogs() {
        try {
            this.writeLog("log.txt", this.logBuffer);
            this.writeLog("log-cmd.txt", this.commandBuffer);

            this.loggerInfo("Log saved!");
        } catch (Exception e) {
            this.loggerWarning(e.getMessage());
        }
    }

    private String getDateTime() {
        long time = System.currentTimeMillis() + Settings.LOGGER_TIMEZONE_OFFSET;
        final SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss >> ");
        return dayTime.format(new Date(time));
    }

    private void writeLog(String fd, StringBuffer buffer) throws IOException {
        BufferedWriter fwLog = new BufferedWriter(new FileWriter(fd, true));
        fwLog.write(buffer.toString());
        fwLog.flush();
        fwLog.close();
        buffer.setLength(0);
    }

}
