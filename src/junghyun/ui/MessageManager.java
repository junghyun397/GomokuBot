package junghyun.ui;

import junghyun.db.DBManager;
import junghyun.ui.languages.MessageEng;
import junghyun.ui.languages.MessageKor;
import junghyun.ui.languages.MessagePrk;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.EmbedBuilder;

import java.util.HashMap;

public class MessageManager {

    public enum LANG {ERR, ENG, KOR, PRK}
    private static HashMap<Long, MessageEng> langList;

    public static final String LANGUAGE_LIST = "`ENG`, `KOR`, `PRK`";
    public static EmbedObject langEmbed;

    public static void loadMessage() {
        MessageManager.langList = new HashMap<>();
        MessageEng.buildMessage();
        MessageKor.buildMessage();
        MessagePrk.buildMessage();

        EmbedBuilder langBuilder = new EmbedBuilder();

        langBuilder.withAuthorName("Language guide");
        langBuilder.withColor(0,145,234);
        langBuilder.withDesc("withDesc");
        langBuilder.withDescription("Please select the appropriate language for this server!");

        langBuilder.appendField("English:flag_us:", "Please use the command `~lang` `ENG`", false);
        langBuilder.appendField("한국어:flag_kr:", "`~lang` `KOR` 명령어를 사용 해주세요.", false);
        langBuilder.appendField("조선어:flag_kp:", "`~lang` `PRK` 명령어를 사용하라우.", false);

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
        }
        return lang;
    }

    private static MessageEng getLanguageInstance(LANG lang) {
        MessageEng rsMessage = new MessageEng();
        switch (lang) {
            case KOR:
                rsMessage = new MessageKor();
                break;
            case PRK:
                rsMessage = new MessagePrk();
                break;
        }
        return rsMessage;
    }

    public static MessageEng getInstance(IGuild guild) {
        MessageEng msgInstance = MessageManager.langList.get(guild.getLongID());
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
