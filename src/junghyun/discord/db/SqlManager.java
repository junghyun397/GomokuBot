package junghyun.discord.db;

import junghyun.discord.Settings;

import java.sql.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SqlManager {

    private final Logger logger;

    private Connection connect;

    public SqlManager(Logger logger) {
        this.logger = logger;
    }

    public void connectMysql() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connect = DriverManager.getConnection(Settings.SQL_URL, Settings.SQL_USER, Settings.SQL_PWD);

            Runnable task = this::validationMysql;
            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
            service.scheduleAtFixedRate(task, 1, 1, TimeUnit.MINUTES);

            this.logger.loggerInfo("mysql connected.");
        } catch (Exception e) {
            this.logger.loggerWarning(e.getMessage());
        }
    }

    public Statement getStatement() {
        try {
            return this.connect.createStatement();
        } catch (SQLException e) {
            this.logger.loggerWarning(e.getLocalizedMessage());
            return null;
        }
    }

    public PreparedStatement getPreparedStatement(String query) {
        try {
            return this.connect.prepareStatement(query);
        } catch (SQLException e) {
            this.logger.loggerWarning(e.getLocalizedMessage());
            return null;
        }
    }

    @SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
    private void validationMysql() {
        try {
            Statement stmt = this.connect.createStatement();
            ResultSet rs = stmt.executeQuery("select 1");
            if (rs.next()) return;
        } catch (SQLException ignored) {}
        this.logger.loggerInfo("mysql-session disconnect detected, start reconnect mysql...");
        this.connectMysql();
    }

}