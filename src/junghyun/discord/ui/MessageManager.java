package junghyun.discord.ui;

import junghyun.discord.db.DBManager;
import junghyun.discord.ui.languages.*;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.EmbedBuilder;

import java.util.HashMap;

public class MessageManager {

    private static HashMap<String, MessageAgent> agentList;
    private static HashMap<Long, MessageAgent> langList;

    public static String baseLanguage = "ENG";

    public static String LanguageList = "";
    public static EmbedObject langEmbed;

    private static EmbedBuilder langBuilder;

    public static void loadMessage() {
        MessageManager.agentList = new HashMap<>();
        MessageManager.langList = new HashMap<>();

        MessageManager.langBuilder = new EmbedBuilder();

        langBuilder.withAuthorName("Language guide");
        langBuilder.withColor(0,145,234);
        langBuilder.withDesc("withDesc");
        langBuilder.withDescription("Please select the appropriate language for this server!");

        // Register Language HERE ↓↓

        MessageManager.registerLanguage(new LanguageENG());
        MessageManager.registerLanguage(new LanguageKOR());
        MessageManager.registerLanguage(new LanguagePRK());
        MessageManager.registerLanguage(new LanguageJPN());
        MessageManager.registerLanguage(new LanguageCHN());
        MessageManager.registerLanguage(new LanguageSKO());

        // ----------------------

        MessageManager.langEmbed = langBuilder.build();
    }

    private static void registerLanguage(LanguageInterface languageContainer) {
        MessageManager.agentList.put(languageContainer.LANGUAGE_CODE(), new MessageAgent(languageContainer));
        MessageManager.LanguageList += "`" + languageContainer.LANGUAGE_CODE() + "` ";
        MessageManager.langBuilder.appendField(languageContainer.LANGUAGE_NAME(), languageContainer.LANGUAGE_DESCRIPTION(), false);
    }

    public static boolean checkLanguage(String language) {
        return MessageManager.getLanguageInstance(language) != null;
    }

    private static MessageAgent getLanguageInstance(String langText) {
        return MessageManager.agentList.get(langText);
    }

    public static MessageAgent getInstance(IGuild guild) {
        MessageAgent msgInstance = MessageManager.langList.get(guild.getLongID());
        if (msgInstance == null) {
            DBManager.GuildDataSet guildDataSet = DBManager.getGuildData(guild.getLongID());
            if (guildDataSet != null)
                MessageManager.langList.put(guild.getLongID(), MessageManager.getLanguageInstance(guildDataSet.getLang()));
            else MessageManager.langList.put(guild.getLongID(), MessageManager.getLanguageInstance(MessageManager.baseLanguage));

            return MessageManager.langList.get(guild.getLongID());
        }
        return msgInstance;
    }

    public static void setLanguage(long id, String lang) {
        MessageManager.langList.put(id, MessageManager.getLanguageInstance(lang));
        DBManager.setGuildLanguage(id, lang);
    }

}
