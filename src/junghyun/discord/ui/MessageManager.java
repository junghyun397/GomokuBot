package junghyun.discord.ui;

import junghyun.discord.db.DBManager;
import junghyun.discord.ui.languages.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageManager {

    final private static HashMap<String, MessageAgent> agentList = new HashMap<>();
    final private static HashMap<Long, MessageAgent> langList = new HashMap<>();

    final private static List<String> styleList = Arrays.asList("A", "B", "C");
    final private static HashMap<Long, String> skinList = new HashMap<>();

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
        MessageManager.registerLanguage(new LanguageVNM());

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

    public static boolean checkSkin(String skin) {
        return MessageManager.styleList.contains(skin);
    }

    private static MessageAgent getLanguageInstance(String langText) {
        return MessageManager.agentList.get(langText);
    }

    public static MessageAgent getInstance(Guild guild) {
        if (MessageManager.langList.containsKey(guild.getIdLong())) return MessageManager.langList.get(guild.getIdLong());

        DBManager.GuildDataSet guildDataSet = DBManager.getGuildData(guild.getIdLong());
        if (guildDataSet != null && guildDataSet.getLang() != null)
            MessageManager.langList.put(guild.getIdLong(), MessageManager.getLanguageInstance(guildDataSet.getLang()));
        else
            MessageManager.langList.put(guild.getIdLong(), MessageManager.getLanguageInstance(MessageManager.languageMap.getOrDefault(guild.getRegionRaw(), MessageManager.baseLanguage)));

        return MessageManager.langList.get(guild.getIdLong());
    }

    public static String getSkin(Guild guild) {
        if (MessageManager.skinList.containsKey(guild.getIdLong())) return MessageManager.skinList.get(guild.getIdLong());

        DBManager.GuildDataSet guildDataSet = DBManager.getGuildData(guild.getIdLong());
        if (guildDataSet != null && guildDataSet.getSkin() != null)
            MessageManager.skinList.put(guild.getIdLong(), guildDataSet.getSkin());
        else
            MessageManager.skinList.put(guild.getIdLong(), "A");

        return MessageManager.skinList.get(guild.getIdLong());
    }

    public static void setLanguage(long id, String lang) {
        MessageManager.langList.put(id, MessageManager.getLanguageInstance(lang));
        DBManager.setGuildLanguage(id, lang);
    }

    public static void setSkin(long id, String skin) {
        MessageManager.skinList.put(id, skin);
        DBManager.setGuildSkin(id, skin);
    }

}
