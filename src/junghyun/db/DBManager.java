package junghyun.db;

import junghyun.unit.ChatGame;
import junghyun.unit.Pos;

import java.sql.ResultSet;

public class DBManager {

    public static void saveGame(ChatGame game) {
        StringBuilder rs = new StringBuilder().append(game.getGame().getTurns()).append(":");
        for (Pos pos: game.getGame().getLog().toArray(new Pos[0])) rs.append(pos.getX()).append(".").append(pos.getY()).append(":");
        rs.append("0000");

        SqlManager.execute("INSERT INTO game_record(record_data, total_count, user_name, date, reason) VALUES ('"
                + rs.toString() + "', " + game.getGame().getTurns() + ", '" + game.getNameTag() + "', " +  (int) System.currentTimeMillis()/100 + ", '"
                + game.getState().toString() + "');");

        UserDataSet orgUser = DBManager.getUserData(game.getNameTag());
        if (orgUser != null) {
            if (game.isWin()) SqlManager.executeUpdate("UPDATE user_info SET win=" + (orgUser.getWin() + 1)
                    + " WHERE name='" + orgUser.getName() + "';");
            else SqlManager.executeUpdate("UPDATE user_info SET lose=" + (orgUser.getLose() + 1)
                    + " WHERE name='" + orgUser.getName() + "';");
        } else {
            if (game.isWin()) SqlManager.execute("INSERT INTO user_info(name, id, win, lose) VALUES ('"
                    + game.getNameTag() + "', " + game.getLongId() + ", 1, 0;");
            else SqlManager.execute("INSERT INTO user_info(name, id, win, lose) VALUES ('"
                    + game.getNameTag() + "', " + game.getLongId() + ", 0, 1;");
        }
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
        ResultSet rs = SqlManager.executeQuery("SELECT * , (SELECT Count(*)+1 FROM user_info WHERE win > t.win ) AS " +
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
