package junghyun.discord.ui;

import junghyun.discord.db.DBManager;
import junghyun.discord.ui.languages.LanguageKOR;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.EmbedBuilder;

import java.util.HashMap;

public class MessageManager {

    public enum LANG {ERR, ENG, KOR, PRK, CHN}
    private static HashMap<Long, MessageAgent> langList;

    public static final String LANGUAGE_LIST = "`ENG`, `KOR`, `PRK`, `CHN`";
    public static EmbedObject langEmbed;

    private static MessageAgent messageAgentENG;
    private static MessageAgent messageAgentKOR;
    private static MessageAgent messageAgentPRK;
    private static MessageAgent messageAgentCHN;

    public static void loadMessage() {
        MessageManager.langList = new HashMap<>();

        MessageManager.messageAgentENG = new MessageAgent(new LanguageKOR());
        MessageManager.messageAgentKOR = new MessageAgent(new LanguageKOR());
        MessageManager.messageAgentPRK = new MessageAgent(new LanguageKOR());
        MessageManager.messageAgentCHN = new MessageAgent(new LanguageKOR());

        EmbedBuilder langBuilder = new EmbedBuilder();

        langBuilder.withAuthorName("Language guide");
        langBuilder.withColor(0,145,234);
        langBuilder.withDesc("withDesc");
        langBuilder.withDescription("Please select the appropriate language for this server!");

        langBuilder.appendField("English:flag_us:", "Please use the command `~lang` `ENG`", false);
        langBuilder.appendField("한국어:flag_kr:", "`~lang` `KOR` 명령어를 사용 해주세요.", false);
        langBuilder.appendField("조선어:flag_kp:", "`~lang` `PRK` 명령문를 사용하라우.", false);
        langBuilder.appendField("汉语:flag_cn: `@kawaii-cirno`", "请使用 `~lang` `CHN` 命令", false);

        MessageManager.langEmbed = langBuilder.build();
    }

    public static LANG getLangByString(String str) {
        LANG lang = LANG.ERR;
        switch (str.toLowerCase()) {
            case "eng":
                lang = LANG.ENG;
                break;
            case "kor":
                lang = LANG.KOR;
                break;
            case "prk":
                lang = LANG.PRK;
                break;
            case "chn":
                lang = LANG.CHN;
                break;
        }
        return lang;
    }

    private static MessageAgent getLanguageInstance(LANG lang) {
        MessageAgent rsMessage;
        switch (lang) {
            case KOR:
                rsMessage = messageAgentKOR;
                break;
            case PRK:
                rsMessage = messageAgentPRK;
                break;
            case CHN:
                rsMessage = messageAgentCHN;
                break;
            default:
                rsMessage = messageAgentENG;
                break;
        }
        return rsMessage;
    }

    public static MessageAgent getInstance(IGuild guild) {
        MessageAgent msgInstance = MessageManager.langList.get(guild.getLongID());
        if (msgInstance == null) {
            DBManager.GuildDataSet guildDataSet = DBManager.getGuildData(guild.getLongID());
            if (guildDataSet != null)
                MessageManager.langList.put(guild.getLongID(), MessageManager.getLanguageInstance(guildDataSet.getLang()));
            else MessageManager.langList.put(guild.getLongID(), MessageManager.getLanguageInstance(LANG.ERR));

            return MessageManager.langList.get(guild.getLongID());
        }
        return msgInstance;
    }

    public static void setLanguage(long id, LANG lang) {
        MessageManager.langList.put(id, MessageManager.getLanguageInstance(lang));
        DBManager.setGuildLanguage(id, lang);
    }

}
