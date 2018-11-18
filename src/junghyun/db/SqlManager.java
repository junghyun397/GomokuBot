package junghyun.db;

import junghyun.unit.Settings;

import java.sql.*;

public class SqlManager {

    private static Connection connect;

    public static void connectMysql() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            SqlManager.connect = DriverManager.getConnection(Settings.SQL_URL, Settings.SQL_USER, Settings.SQL_PWD);
            Logger.loggerInfo("Mysql connected!");
        } catch (Exception e) {
            Logger.loggerWarning(e.getMessage());
        }
    }

    static ResultSet executeQuery(String query) {
        try {
            PreparedStatement pstmt = SqlManager.connect.prepareStatement(query);
            return pstmt.executeQuery(query);
        } catch (SQLException e) {
            Logger.loggerWarning(e.getMessage());
            return null;
        }
    }

    static void executeUpdate(String query) {
        try {
            PreparedStatement pstmt = SqlManager.connect.prepareStatement(query);
            pstmt.executeUpdate(query);
            pstmt.close();
        } catch (SQLException e) {
            Logger.loggerWarning(e.getMessage());
        }
    }

    static void execute(String query) {
        try {
            PreparedStatement pstmt = SqlManager.connect.prepareStatement(query);
            pstmt.execute(query);
            pstmt.close();
        } catch (SQLException e) {
            Logger.loggerWarning(e.getMessage());
        }
    }

}