package junghyun.discord.ui;

import junghyun.discord.db.DBManager;
import junghyun.discord.ui.languages.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    final private static HashMap<String, MessageAgent> agentList = new HashMap<>();
    final private static HashMap<Long, MessageAgent> langList = new HashMap<>();

    final private static Map<String, String> languageMap = new HashMap<>();
    final private static String baseLanguage = "ENG";

    public static String LanguageList = "";
    public static MessageEmbed langEmbed;

    private static EmbedBuilder langBuilder;

    public static void loadMessage() {
        MessageManager.langBuilder = new EmbedBuilder();

        langBuilder.setAuthor("Language guide");
        langBuilder.setColor(new Color(0, 145, 234));
        langBuilder.setDescription("The default language has been set as the server region for this channel. Please select the proper language for this server!");

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
        for (String targetRegion: languageContainer.TARGET_REGION())
            if (!MessageManager.languageMap.containsKey(targetRegion))
                MessageManager.languageMap.put(targetRegion, languageContainer.LANGUAGE_CODE());
        MessageManager.agentList.put(languageContainer.LANGUAGE_CODE(), new MessageAgent(languageContainer));
        MessageManager.LanguageList += "`" + languageContainer.LANGUAGE_CODE() + "` ";
        MessageManager.langBuilder.addField(languageContainer.LANGUAGE_NAME(), languageContainer.LANGUAGE_DESCRIPTION(), false);
    }

    public static boolean checkLanguage(String language) {
        return MessageManager.agentList.containsKey(language);
    }

    private static MessageAgent getLanguageInstance(String langText) {
        return MessageManager.agentList.get(langText);
    }

    public static MessageAgent getInstance(Guild guild) {
        final MessageAgent msgInstance = MessageManager.langList.get(guild.getIdLong());
        if (msgInstance == null) {
            DBManager.GuildDataSet guildDataSet = DBManager.getGuildData(guild.getIdLong());
            if (guildDataSet != null)
                MessageManager.langList.put(
                        guild.getIdLong(),
                        MessageManager.getLanguageInstance(guildDataSet.getLang())
                );
            else
                MessageManager.langList.put(
                        guild.getIdLong(),
                        MessageManager.getLanguageInstance(
                                MessageManager.languageMap.getOrDefault(guild.getRegionRaw(), MessageManager.baseLanguage)
                        )
                );

            return MessageManager.langList.get(guild.getIdLong());
        }
        return msgInstance;
    }

    public static void setLanguage(long id, String lang) {
        MessageManager.langList.put(id, MessageManager.getLanguageInstance(lang));
        DBManager.setGuildLanguage(id, lang);
    }

}
