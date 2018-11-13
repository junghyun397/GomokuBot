package junghyun.db;

import junghyun.unit.ChatGame;

import java.sql.ResultSet;

public class DBManager {

    public static void saveGame(ChatGame game) {
        String query = " ";
        ResultSet rs = SqlManager.executeQuery(query);
    }
}
