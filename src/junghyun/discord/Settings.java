package junghyun.discord;

public class Settings {

    public static final String TOKEN = PrivateSettings.TOKEN;

    public static final char PREFIX = '~';

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

    public static final String VERSION = "v4.2.1";

    public static final long OFFICIAL_GUILD_ID = PrivateSettings.OFFICIAL_GUILD_ID;
    public static final long RESULT_CHANNEL_ID = PrivateSettings.RESULT_CHANNEL_ID;

}
