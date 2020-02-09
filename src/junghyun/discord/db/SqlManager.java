package junghyun.discord.db;

import junghyun.discord.Settings;

import java.sql.*;

public class SqlManager {

    private static Connection connect;

    public static void connectMysql() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            SqlManager.connect = DriverManager.getConnection(Settings.SQL_URL, Settings.SQL_USER, Settings.SQL_PWD);
            Logger.loggerInfo("mysql connected.");
        } catch (Exception e) {
            Logger.loggerWarning(e.getMessage());
        }
    }

    @SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
    private static void validationMysql() {
        try {
            PreparedStatement pstmt = SqlManager.connect.prepareStatement("select 1");
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return;
        } catch (SQLException ignored) {}
        Logger.loggerInfo("mysql-session disconnect detected, start reconnect mysql...");
        SqlManager.connectMysql();
    }

    static ResultSet executeQuery(String query) {
        try {
            SqlManager.validationMysql();
            PreparedStatement pstmt = SqlManager.connect.prepareStatement(query);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            Logger.loggerWarning(e.getMessage());
            return null;
        }
    }

    static void executeUpdate(String query) {
        try {
            SqlManager.validationMysql();
            PreparedStatement pstmt = SqlManager.connect.prepareStatement(query);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            Logger.loggerWarning(e.getMessage());
        }
    }

    static void execute(String query) {
        try {
            SqlManager.validationMysql();
            PreparedStatement pstmt = SqlManager.connect.prepareStatement(query);
            pstmt.execute();
            pstmt.close();
        } catch (SQLException e) {
            Logger.loggerWarning(e.getMessage());
        }
    }

}