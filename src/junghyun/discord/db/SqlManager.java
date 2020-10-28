package junghyun.discord.db;

import junghyun.discord.Settings;

import java.sql.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SqlManager {

    private static Connection connect;

    public static void connectMysql() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            SqlManager.connect = DriverManager.getConnection(Settings.SQL_URL, Settings.SQL_USER, Settings.SQL_PWD);

            Runnable task = SqlManager::validationMysql;
            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
            service.scheduleAtFixedRate(task, 1, 1, TimeUnit.MINUTES);

            Logger.loggerInfo("mysql connected.");
        } catch (Exception e) {
            Logger.loggerWarning(e.getMessage());
        }
    }

    public static Statement getStatement() {
        try {
            return SqlManager.connect.createStatement();
        } catch (SQLException e) {
            Logger.loggerWarning(e.getLocalizedMessage());
            return null;
        }
    }

    public static PreparedStatement getPreparedStatement(String query) {
        try {
            return SqlManager.connect.prepareStatement(query);
        } catch (SQLException e) {
            Logger.loggerWarning(e.getLocalizedMessage());
            return null;
        }
    }

    @SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
    private static void validationMysql() {
        try {
            Statement stmt = SqlManager.connect.createStatement();
            ResultSet rs = stmt.executeQuery("select 1");
            if (rs.next()) return;
        } catch (SQLException ignored) {}
        Logger.loggerInfo("mysql-session disconnect detected, start reconnect mysql...");
        SqlManager.connectMysql();
    }

}