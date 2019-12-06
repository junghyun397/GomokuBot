package junghyun.discord.db;

import junghyun.ai.Pos;
import junghyun.discord.game.ChatGame;
import junghyun.discord.game.OppPlayer;

import java.sql.ResultSet;

public class DBManager {

    public static void saveGame(ChatGame game) {
        StringBuilder rs = new StringBuilder().append(game.getGame().getTurns()).append(":");
        for (Pos pos: game.getGame().getLog().toArray(new Pos[0])) rs.append(pos.getX()).append(".").append(pos.getY()).append(":");
        rs.append("0000");

        SqlManager.execute("INSERT INTO game_record(record_data, total_count, user_id, date, reason) VALUES ('"
                + rs.toString() + "', " + game.getGame().getTurns() + ", " + game.getLongId() + ", " + System.currentTimeMillis() + ", '"
                + game.getState().toString() + "');");

        if (game.getOppPlayer().getPlayerType() == OppPlayer.PLAYER_TYPE.HUMAN) return;

        UserDataSet orgUser = DBManager.getUserData(game.getLongId());
        if (orgUser != null) {
            if (game.isWin()) SqlManager.executeUpdate("UPDATE user_info SET win=" + (orgUser.getWin() + 1)
                    + " WHERE user_id=" + orgUser.getLongId() + ";");
            else SqlManager.executeUpdate("UPDATE user_info SET lose=" + (orgUser.getLose() + 1)
                    + " WHERE user_id=" + orgUser.getLongId() + ";");
        } else {
            if (game.isWin()) SqlManager.execute("INSERT INTO user_info(user_id, name_tag, win, lose) VALUES ("
                    + game.getLongId() + ", '" + game.getNameTag() + "', 1, 0);");
            else SqlManager.execute("INSERT INTO user_info(user_id, name_tag, win, lose) VALUES ("
                    + game.getLongId() + ", '" + game.getNameTag() + "', 0, 1);");
        }
    }

    public static UserDataSet getUserData(long id) {
        ResultSet rs = SqlManager.executeQuery("SELECT * FROM user_info WHERE user_id = '" + id + "';");
        try {
            assert rs != null;
            if (!rs.next()) return null;
            return new UserDataSet(id, rs.getString("name_tag"), rs.getInt("win"), rs.getInt("lose"));
        } catch (Exception e) {
            Logger.loggerWarning(e.getMessage());
            return null;
        }
    }

    public static UserDataSet[] getRankingData(int count, long targetID) {
        ResultSet rs = SqlManager.executeQuery("SELECT *, (@rank := @rank + 0) AS rank FROM user_info AS a, " +
                "(SELECT @rank := 0) AS b ORDER BY a.win DESC");

        UserDataSet[] rsDataSet = new UserDataSet[count + 1];
        try {
            int idx = 1;
            assert rs != null;
            while (rs.next()) {
                if (idx < count + 1)
                    rsDataSet[idx] = new UserDataSet(rs.getLong("user_id"), rs.getString("name_tag"),
                            rs.getInt("win"), rs.getInt("lose"));
                idx++;

                if (rs.getLong("user_id") == targetID)
                    rsDataSet[0] = new UserDataSet(idx - 1, rs.getString("name_tag"),
                            rs.getInt("win"), rs.getInt("lose"));
            }
            return rsDataSet;
        } catch (Exception e) {
            Logger.loggerWarning(e.getMessage());
            return null;
        }
    }

    public static void setGuildLanguage(long id, String lang) {
        GuildDataSet orgGuild = DBManager.getGuildData(id);
        if (orgGuild != null) {
            SqlManager.executeUpdate("UPDATE guild_info SET lang='" + lang + "' WHERE guild_id=" + id + ";");
        } else {
            SqlManager.execute("INSERT INTO guild_info(guild_id, lang) VALUES (" + id + ", '" + lang + "');");
        }
    }

    public static GuildDataSet getGuildData(long id) {
        ResultSet rs = SqlManager.executeQuery("SELECT * FROM guild_info WHERE guild_id = '" + id + "';");
        try {
            assert rs != null;
            if (!rs.next()) return null;
            return new GuildDataSet(rs.getLong("guild_id"), rs.getString("lang"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class UserDataSet {

        private long longId;
        private String name;

        private int win;
        private int lose;

        private UserDataSet(long id, String name, int win, int lose) {
            this.longId = id;
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

        public long getLongId() {
            return this.longId;
        }
    }

    public static class GuildDataSet {

        private long longId;

        private String lang;

        private GuildDataSet(long id, String lang) {
            this.longId = id;
            this.lang = lang;
        }

        public long getLongId() {
            return longId;
        }

        public String getLang() {
            return lang;
        }
    }
}
