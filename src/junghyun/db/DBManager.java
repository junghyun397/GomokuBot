package junghyun.db;

import junghyun.unit.ChatGame;
import junghyun.unit.Pos;

import java.sql.ResultSet;

public class DBManager {

    public static void saveGame(ChatGame game) {
        StringBuilder rs = new StringBuilder(game.getGame().getTurns()).append(":");
        for (Pos pos: (Pos[]) game.getGame().getLog().toArray()) rs.append(pos.getX()).append(".").append(pos.getY());

        SqlManager.execute("INSERT INTO user_info(record_data, total_count, user_name) VALUES (" +
                "'" + rs.toString() + "', '" + game.getGame().getTurns() + "', '" + game.getNameTag() + "');");
    }

    public static UserDataSet getUserData(String name) {
        ResultSet rs = SqlManager.executeQuery("SELECT * FROM user_info WHERE name = '" + name + "';");
        try {
            assert rs != null;
            if (!rs.next()) return null;
            return new UserDataSet(name, rs.getInt("win"), rs.getInt("lose"));
        } catch (Exception e) {
            Logger.loggerWarning(e.getMessage());
            return null;
        }
    }

    public static UserDataSet[] getRankingData(int count) {
        ResultSet rs = SqlManager.executeQuery("SELECT * , (SELECT Count(*)+1 FROM user_info WHERE win > t.win ) As " +
                "temp_rank FROM user_info AS t ORDER BY temp_rank LIMIT 0, " + count + ";");

        UserDataSet[] rsDataSet = new UserDataSet[count];

        try {
            for (int i = 0; i < count; i++) {
                assert rs != null;
                rs.next();
                rsDataSet[i] = new UserDataSet(rs.getString("name"),
                        rs.getInt("win"), rs.getInt("lose"));
            }
            return rsDataSet;
        } catch (Exception e) {
            Logger.loggerWarning(e.getMessage());
            return null;
        }
    }

    public static class UserDataSet {

        private String name;

        private int win;
        private int lose;

        private UserDataSet(String name, int win, int lose) {
            this.name = name;
            this.win = win;
            this.lose = lose;
        }

        public String getName() {
            return name;
        }

        public int getWin() {
            return win;
        }

        public int getLose() {
            return lose;
        }
    }
}
