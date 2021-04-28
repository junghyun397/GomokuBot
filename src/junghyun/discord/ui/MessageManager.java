package junghyun.discord.ui;

import junghyun.discord.BotManager;
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

@SuppressWarnings("FieldCanBeLocal")
public class MessageManager {

    private final BotManager botManager;
    private final DBManager dbManager;

    private final HashMap<String, MessageAgent> agentList = new HashMap<>();
    private final HashMap<Long, MessageAgent> langList = new HashMap<>();

    private final List<String> styleList = Arrays.asList("A", "B", "C");
    private final HashMap<Long, String> skinList = new HashMap<>();

    private final Map<String, String> languageMap = new HashMap<>();
    private final String baseLanguage = "ENG";

    public String LanguageList = "";
    public MessageEmbed langEmbed;

    private EmbedBuilder langBuilder;

    public MessageManager(BotManager botManager, DBManager dbManager) {
        this.botManager = botManager;
        this.dbManager = dbManager;
    }

    public void loadMessages() {
        this.langBuilder = new EmbedBuilder();

        langBuilder.setAuthor("Language guide");
        langBuilder.setColor(new Color(0, 145, 234));
        langBuilder.setDescription("The default language has been set as the server region for this channel. Please select the proper language for this server!");

        // Register Language HERE ↓↓

        this.registerLanguage(new LanguageENG());
        this.registerLanguage(new LanguageKOR());
        this.registerLanguage(new LanguagePRK());
        this.registerLanguage(new LanguageJPN());
        this.registerLanguage(new LanguageCHN());
        this.registerLanguage(new LanguageSKO());
        this.registerLanguage(new LanguageVNM());

        // ----------------------

        this.langEmbed = langBuilder.build();
    }

    private void registerLanguage(LanguageInterface languageContainer) {
        for (String targetRegion: languageContainer.TARGET_REGION())
            if (!this.languageMap.containsKey(targetRegion))
                this.languageMap.put(targetRegion, languageContainer.LANGUAGE_CODE());
        this.agentList.put(languageContainer.LANGUAGE_CODE(), new MessageAgent(this, botManager, dbManager, languageContainer));
        this.LanguageList += "`" + languageContainer.LANGUAGE_CODE() + "` ";
        this.langBuilder.addField(languageContainer.LANGUAGE_NAME(), languageContainer.LANGUAGE_DESCRIPTION(), false);
    }

    public boolean checkLanguage(String language) {
        return this.agentList.containsKey(language);
    }

    public boolean checkSkin(String skin) {
        return this.styleList.contains(skin);
    }

    private MessageAgent getLanguageInstance(String langText) {
        return this.agentList.get(langText);
    }

    public MessageAgent getAgent(Guild guild) {
        if (this.langList.containsKey(guild.getIdLong())) return this.langList.get(guild.getIdLong());

        DBManager.GuildDataSet guildDataSet = this.dbManager.getGuildData(guild.getIdLong());
        if (guildDataSet != null && guildDataSet.getLang() != null)
            this.langList.put(guild.getIdLong(), this.getLanguageInstance(guildDataSet.getLang()));
        else
            this.langList.put(guild.getIdLong(), this.getLanguageInstance(this.languageMap.getOrDefault(guild.getRegionRaw(), this.baseLanguage)));

        return this.langList.get(guild.getIdLong());
    }

    public String getSkin(Guild guild) {
        if (this.skinList.containsKey(guild.getIdLong())) return this.skinList.get(guild.getIdLong());

        DBManager.GuildDataSet guildDataSet = this.dbManager.getGuildData(guild.getIdLong());
        if (guildDataSet != null && guildDataSet.getSkin() != null)
            this.skinList.put(guild.getIdLong(), guildDataSet.getSkin());
        else
            this.skinList.put(guild.getIdLong(), "A");

        return this.skinList.get(guild.getIdLong());
    }

    public void setLanguage(long id, String lang) {
        this.langList.put(id, this.getLanguageInstance(lang));
        this.dbManager.setGuildLanguage(id, lang);
    }

    public void setSkin(long id, String skin) {
        this.skinList.put(id, skin);
        this.dbManager.setGuildSkin(id, skin);
    }

}
