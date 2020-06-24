package junghyun.discord;

/*
PrivateSettings.java

package junghyun.discord;

class PrivateSettings {

    static final String TOKEN = "TOKEN";

    static final int LOGGER_TIMEZONE_OFFSET = 0;

    static final String SQL_URL = "jdbc:mysql://localhost:3306/gomokubot";
    static final String SQL_USER = "user";
    static final String SQL_PWD = "password";

    static final String VERSION = "version";

    static final long OFFICIAL_GUILD_ID = 0L;
    static final long RESULT_CHANNEL_ID = 0L;

}

 */

public class Settings {

    public static final String TOKEN = PrivateSettings.TOKEN;

    public static final char PREFIX = '~';

    public static final int LOGGER_TIMEZONE_OFFSET = PrivateSettings.LOGGER_TIMEZONE_OFFSET; // O to set to UTC

    public static final int LOGGER_SAVE = 3600;

    public static final int TIMEOUT_CYCLE = 1800;
    public static final int TIMEOUT = TIMEOUT_CYCLE*1000;

    public static final String SQL_URL = PrivateSettings.SQL_URL +
            "?autoReconnection=true" +
            "&useUnicode=true" +
            "&characterEncoding=UTF-8" +
            "&serverTimezone=Asia/Seoul";
    public static final String SQL_USER = PrivateSettings.SQL_USER;
    public static final String SQL_PWD = PrivateSettings.SQL_PWD;

    public static final int RANK_COUNT = 10;

    public static final String VERSION = PrivateSettings.VERSION;

    public static final long OFFICIAL_GUILD_ID = PrivateSettings.OFFICIAL_GUILD_ID;
    public static final long RESULT_CHANNEL_ID = PrivateSettings.RESULT_CHANNEL_ID;

}
