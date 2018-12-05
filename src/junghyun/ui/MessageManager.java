package junghyun.ui;

import junghyun.db.DBManager;
import junghyun.ui.languages.MessageCHN;
import junghyun.ui.languages.MessageENG;
import junghyun.ui.languages.MessageKOR;
import junghyun.ui.languages.MessagePRK;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.EmbedBuilder;

import java.util.HashMap;

public class MessageManager {

    public enum LANG {ERR, ENG, KOR, PRK, CHN}
    private static HashMap<Long, MessageENG> langList;

    public static final String LANGUAGE_LIST = "`ENG`, `KOR`, `PRK`, `CHN`";
    public static EmbedObject langEmbed;

    public static void loadMessage() {
        MessageManager.langList = new HashMap<>();

        MessageENG.buildMessage();
        MessageKOR.buildMessage();
        MessagePRK.buildMessage();
        MessageCHN.buildMessage();

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

    private static MessageENG getLanguageInstance(LANG lang) {
        MessageENG rsMessage = new MessageENG();
        switch (lang) {
            case KOR:
                rsMessage = new MessageKOR();
                break;
            case PRK:
                rsMessage = new MessagePRK();
                break;
            case CHN:
                rsMessage = new MessageCHN();
                break;
        }
        return rsMessage;
    }

    public static MessageENG getInstance(IGuild guild) {
        MessageENG msgInstance = MessageManager.langList.get(guild.getLongID());
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
