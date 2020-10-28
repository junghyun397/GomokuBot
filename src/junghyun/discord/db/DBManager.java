package junghyun.discord.db;

import junghyun.ai.Pos;
import junghyun.discord.game.ChatGame;
import junghyun.discord.game.OppPlayer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManager {

    public static void saveGame(ChatGame game) {
        StringBuilder rs = new StringBuilder().append(game.getGame().getTurns()).append(":");
        for (Pos pos: game.getGame().getLog().toArray(new Pos[0])) rs.append(pos.getX()).append(".").append(pos.getY()).append(":");
        rs.append("0000");

        try {
            final PreparedStatement gpstmt = SqlManager.getPreparedStatement(
                    "INSERT INTO game_record(record_data, total_count, user_id, date, reason) VALUES (?, ?, ?, ?, ?);");
            assert gpstmt != null;

            gpstmt.setString(1, rs.toString());
            gpstmt.setInt(2, game.getGame().getTurns());
            gpstmt.setLong(3, game.getLongId());
            gpstmt.setLong(4, System.currentTimeMillis());
            gpstmt.setString(5, game.getState().toString());

            gpstmt.execute();
            gpstmt.clearParameters();

            if (game.getOppPlayer().getPlayerType() == OppPlayer.PLAYER_TYPE.HUMAN) return;

            UserDataSet orgUser = DBManager.getUserData(game.getLongId());
            if (orgUser != null) {
                if (game.isWin()) {
                    final PreparedStatement pstmt = SqlManager.getPreparedStatement(
                            "UPDATE user_info SET win=? WHERE user_id=?;");
                    assert pstmt != null;

                    pstmt.setInt(1, orgUser.getWin() + 1);
                    pstmt.setLong(2, orgUser.getLongId());

                    pstmt.execute();
                    pstmt.clearParameters();
                } else {
                    final PreparedStatement pstmt =
                            SqlManager.getPreparedStatement(
                                    "UPDATE user_info SET lose=? WHERE user_id=?;");
                    assert pstmt != null;

                    pstmt.setInt(1, orgUser.getLose() + 1);
                    pstmt.setLong(2, orgUser.getLongId());

                    pstmt.execute();
                    pstmt.clearParameters();
                }
            } else {
                if (game.isWin()) {
                    final PreparedStatement pstmt = SqlManager.getPreparedStatement(
                            "INSERT INTO user_info(user_id, name_tag, win, lose) VALUES (?, ?, 1, 0);");
                    assert pstmt != null;

                    pstmt.setLong(1, game.getLongId());
                    pstmt.setString(2, game.getNameTag());

                    pstmt.execute();
                    pstmt.clearParameters();
                } else {
                    final PreparedStatement pstmt = SqlManager.getPreparedStatement(
                            "INSERT INTO user_info(user_id, name_tag, win, lose) VALUES (?, ?, 0, 1);");
                    assert pstmt != null;

                    pstmt.setLong(1, game.getLongId());
                    pstmt.setString(2, game.getNameTag());

                    pstmt.execute();
                    pstmt.clearParameters();
                }
            }
        } catch (SQLException e) {
            Logger.loggerWarning(e.getMessage());
        }
    }

    public static UserDataSet getUserData(long id) {
        try {
            final PreparedStatement pstmt = SqlManager.getPreparedStatement(
                    "SELECT * FROM user_info WHERE user_id=?;");
            assert pstmt != null;

            pstmt.setLong(1, id);

            ResultSet rs = pstmt.executeQuery();
            assert rs != null;

            if (!rs.next()) return null;
            return new UserDataSet(id, rs.getString("name_tag"), rs.getInt("win"), rs.getInt("lose"));
        } catch (SQLException e) {
            Logger.loggerWarning(e.getMessage());
            return null;
        }
    }

    public static UserDataSet[] getRankingData(int count, long targetID) {
        try {
            final Statement stmt = SqlManager.getStatement();
            assert stmt != null;
            ResultSet rs = stmt.executeQuery("SELECT *, (@rank := @rank + 0) AS rank FROM user_info AS a, " +
                    "(SELECT @rank := 0) AS b ORDER BY a.win DESC");
            assert rs != null;

            UserDataSet[] rsDataSet = new UserDataSet[count + 1];

            int idx = 1;
            while (rs.next()) {
                if (idx < count + 1)
                    rsDataSet[idx] = new UserDataSet(rs.getLong("user_id"), rs.getString("name_tag"), rs.getInt("win"), rs.getInt("lose"));
                idx++;

                if (rs.getLong("user_id") == targetID)
                    rsDataSet[0] = new UserDataSet(idx - 1, rs.getString("name_tag"), rs.getInt("win"), rs.getInt("lose"));
            }
            return rsDataSet;
        } catch (SQLException e) {
            Logger.loggerWarning(e.getMessage());
            return null;
        }
    }

    public static void setGuildLanguage(long id, String lang) {
        GuildDataSet orgGuild = DBManager.getGuildData(id);
        try {
            if (orgGuild != null) {
                final PreparedStatement upstmt = SqlManager.getPreparedStatement(
                        "UPDATE guild_info SET lang=? WHERE guild_id=?;");
                assert upstmt != null;

                upstmt.setString(1, lang);
                upstmt.setLong(2, id);

                upstmt.executeUpdate();
                upstmt.clearParameters();
            } else {
                final PreparedStatement ipstmt = SqlManager.getPreparedStatement(
                        "INSERT INTO guild_info(guild_id, lang) VALUES (?, ?);");
                assert ipstmt != null;

                ipstmt.setLong(1, id);
                ipstmt.setString(2, lang);

                ipstmt.execute();
                ipstmt.clearParameters();
            }
        } catch (SQLException e) {
            Logger.loggerWarning(e.getMessage());
        }
    }

    public static void setGuildSkin(long id, String skin) {
        GuildDataSet orgGuild = DBManager.getGuildData(id);
        try {
            if (orgGuild != null) {
                final PreparedStatement upstmt = SqlManager.getPreparedStatement(
                        "UPDATE guild_info SET skin=? WHERE guild_id=?;");
                assert upstmt != null;

                upstmt.setString(1, skin);
                upstmt.setLong(2, id);

                upstmt.executeUpdate();
                upstmt.clearParameters();
            } else {
                final PreparedStatement ipstmt = SqlManager.getPreparedStatement(
                        "INSERT INTO guild_info(guild_id, skin) VALUES (?, ?);");
                assert ipstmt != null;

                ipstmt.setLong(1, id);
                ipstmt.setString(1, skin);

                ipstmt.execute();
                ipstmt.clearParameters();
            }
        } catch (SQLException e) {
            Logger.loggerWarning(e.getMessage());
        }
    }

    public static GuildDataSet getGuildData(long id) {
        try {
            final Statement stmt = SqlManager.getStatement();
            assert stmt != null;
            ResultSet rs = stmt.executeQuery("SELECT * FROM guild_info WHERE guild_id = '" + id + "';");

            if (!rs.next()) return null;
            return new GuildDataSet(rs.getLong("guild_id"), rs.getString("lang"), rs.getString("skin"));
        } catch (Exception e) {
            Logger.loggerWarning(e.getMessage());
            return null;
        }
    }

    public static class UserDataSet {

        private final long longId;
        private final String name;

        private final int win;
        private final int lose;

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

        private final long longId;

        private final String lang;

        private final String skin;

        private GuildDataSet(long id, String lang, String skin) {
            this.longId = id;
            this.lang = lang;
            this.skin = skin;
        }

        public long getLongId() {
            return longId;
        }

        public String getLang() {
            return lang;
        }

        public String getSkin() {
            return skin;
        }
    }
}
