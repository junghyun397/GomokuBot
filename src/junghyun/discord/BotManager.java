package junghyun.discord;

import junghyun.ai.Pos;
import junghyun.discord.db.DBManager;
import junghyun.discord.db.Logger;
import junghyun.discord.db.SqlManager;
import junghyun.discord.ui.MessageManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.security.auth.login.LoginException;
import java.util.Objects;
import java.util.function.Consumer;

public class BotManager {

    private final Logger logger;
    private final DBManager dbManager;
    private final MessageManager messageManager;
    private final GameManager gameManager;

    private JDA client;

    public BotManager(Logger logger, DBManager dbManager) {
        this.logger = logger;
        this.dbManager = dbManager;
        this.messageManager = new MessageManager(this, dbManager);
        this.gameManager = new GameManager(this, dbManager, logger, messageManager);
    }

    public void startBotManager() throws LoginException, InterruptedException {
        JDABuilder builder = JDABuilder.createDefault(Settings.TOKEN);
        builder.setBulkDeleteSplittingEnabled(false);
        builder.setActivity(Activity.watching("~help"));

        builder.addEventListeners(new EventListener(this, logger, messageManager));

        client = builder.build();
        client.awaitReady();

        this.gameManager.startGameManager();
        this.messageManager.loadMessages();
    }

    public void endGomokuBot() {
        this.client.shutdown();
    }

    void processCommand(MessageReceivedEvent event) {
        final Consumer<Message> addReactionCheck = (message) -> {
            if (event.getGuild().getSelfMember().getPermissions().contains(Permission.MESSAGE_ADD_REACTION)) message.addReaction("\u2611\uFE0F").queue();
        };

        final Consumer<Message> addReactionCrossMark = (message) -> {
            if (event.getGuild().getSelfMember().getPermissions().contains(Permission.MESSAGE_ADD_REACTION)) message.addReaction("\u274C").queue();
        };

        final Consumer<Boolean> reactionAgent = (result) -> {
            if (result) addReactionCheck.accept(event.getMessage());
            else addReactionCrossMark.accept(event.getMessage());
        };

        if (event.getMessage().getContentDisplay().isEmpty() || !event.getTextChannel().canTalk()) return;

        String[] splitText = event.getMessage().getContentDisplay().toLowerCase().split(" ");
        this.logger.loggerCommand(event.getAuthor().getName() + " : " + event.getAuthor().getName()
                + " / " + event.getMessage().getContentDisplay());

        switch (splitText[0]) {
            case "~help":
                addReactionCheck.accept(event.getMessage());
                this.messageManager.getAgent(event.getGuild()).sendHelp(event.getTextChannel());
                break;
            case "~lang":
                if (splitText.length != 2) {
                    addReactionCrossMark.accept(event.getMessage());
                    this.messageManager.getAgent(event.getGuild()).sendLanguageChange(event.getTextChannel(), null);
                    break;
                }
                String lang = splitText[1].toUpperCase();

                if (this.messageManager.checkLanguage(lang)) this.messageManager.setLanguage(event.getGuild().getIdLong(), lang);
                else lang = null;

                if (lang != null) addReactionCheck.accept(event.getMessage());
                else addReactionCrossMark.accept(event.getMessage());
                this.messageManager.getAgent(event.getGuild()).sendLanguageChange(event.getTextChannel(), lang);
                break;
            case "~skin":
                if (splitText.length != 2) {
                    addReactionCrossMark.accept(event.getMessage());
                    this.messageManager.getAgent(event.getGuild()).sendSkinChange(event.getTextChannel(), null);
                    break;
                }
                String skin = splitText[1].toUpperCase();

                if (this.messageManager.checkSkin(skin)) this.messageManager.setSkin(event.getGuild().getIdLong(), skin);
                else skin = null;

                if (skin != null) addReactionCheck.accept(event.getMessage());
                else addReactionCrossMark.accept(event.getMessage());
                this.messageManager.getAgent(event.getGuild()).sendSkinChange(event.getTextChannel(), skin);
                break;
            case "~rank":
                addReactionCheck.accept(event.getMessage());
                this.messageManager.getAgent(event.getGuild()).sendRank(event.getAuthor(), event.getTextChannel(),
                        Objects.requireNonNull(this.dbManager.getRankingData(Settings.RANK_COUNT, event.getAuthor().getIdLong())));
                break;
            case "~start":
                User targetUser = null;
                if (event.getMessage().getMentionedUsers().size() > 0) targetUser = event.getMessage().getMentionedUsers().get(0);

                this.gameManager.createGame(event.getAuthor(), event.getTextChannel(), targetUser, reactionAgent);
                break;
            case "~resign":
                this.gameManager.resignGame(event.getAuthor(), event.getTextChannel(), reactionAgent);
                break;
            case "~s":
                if ((splitText.length != 3)
                        || (!((splitText[1].length() == 1) && ((splitText[2].length() == 1)
                        || (splitText[2].length() == 2))))) {
                    addReactionCrossMark.accept(event.getMessage());
                    this.messageManager.getAgent(event.getGuild()).sendSyntaxError(event.getAuthor(), event.getTextChannel());
                    break;
                }

                Pos pos;
                try {
                    pos = new Pos(Pos.engToInt(splitText[1].toLowerCase().toCharArray()[0]), Integer.parseInt(splitText[2].toLowerCase()) - 1);
                } catch (Exception e) {
                    addReactionCrossMark.accept(event.getMessage());
                    this.messageManager.getAgent(event.getGuild()).sendSyntaxError(event.getAuthor(), event.getTextChannel());
                    break;
                }

                if (!Pos.checkSize(pos.getX(), pos.getY())) {
                    addReactionCrossMark.accept(event.getMessage());
                    this.messageManager.getAgent(event.getGuild()).sendSyntaxError(event.getAuthor(), event.getTextChannel());
                    break;
                }

                this.gameManager.putStone(pos, event.getAuthor(), event.getTextChannel(), reactionAgent);
                break;
        }
    }

    public JDA getClient() {
        return this.client;
    }

    public GameManager getGameManager() {
        return this.gameManager;
    }

    public TextChannel getOfficialChannel() {
        return Objects.requireNonNull(this.getClient().getGuildById(Settings.OFFICIAL_GUILD_ID)).getTextChannelById(Settings.RESULT_CHANNEL_ID);
    }

}
