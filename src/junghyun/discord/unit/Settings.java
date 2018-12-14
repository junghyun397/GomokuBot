package junghyun.discord.unit;

public class Settings {

    public static final String TOKEN = PrivateSettings.TOKEN;

    public static final char PREFIX = '~';

    public static final int LOGGER_SAVE = 3600;

    public static final int TIMEOUT_CYCLE = 1800;
    public static final int TIMEOUT = TIMEOUT_CYCLE*1000;

    public static final String SQL_URL = PrivateSettings.SQL_URL +
            "?autoReconnect=true" +
            "&useUnicode=true" +
            "&characterEncoding=UTF-8";
    public static final String SQL_USER = PrivateSettings.SQL_USER;
    public static final String SQL_PWD = PrivateSettings.SQL_PWD;

    public static final int RANK_COUNT = 10;

    public static final String VERSION = "alpha v2.5.0";

}
