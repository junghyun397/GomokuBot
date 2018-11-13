package junghyun.db;

import junghyun.unit.Settings;

import java.sql.*;

public class SqlManager {

    private static Connection connect;

    public static void connectMysql() {
        try {
            SqlManager.connect = DriverManager.getConnection(Settings.SQL_URL, Settings.SQL_USER, Settings.SQL_PWD);
        } catch (SQLException e) {
            Logger.loggerWarning(e.getMessage());
        }
    }

    public static ResultSet executeQuery(String query) {
        try {
            PreparedStatement pstmt = SqlManager.connect.prepareStatement(query);
            return pstmt.executeQuery(query);
        } catch (SQLException e) {
            Logger.loggerWarning(e.getMessage());
            return null;
        }
    }

    public static int executeUpdate(String query) {
        try {
            PreparedStatement pstmt = SqlManager.connect.prepareStatement(query);
            int result = pstmt.executeUpdate(query);
            pstmt.close();
            return result;
        } catch (SQLException e) {
            Logger.loggerWarning(e.getMessage());
            return 0;
        }
    }

    public static boolean execute(String query) {
        try {
            PreparedStatement pstmt = SqlManager.connect.prepareStatement(query);
            boolean result = pstmt.execute(query);
            pstmt.close();
            return result;
        } catch (SQLException e) {
            Logger.loggerWarning(e.getMessage());
            return false;
        }
    }

}