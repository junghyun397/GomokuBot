package junghyun.discord.ui.languages;

public class LanguageENG implements LanguageInterface {

    @Override
    public String[] TARGET_REGION() {
        return new String[]{};
    }

    @Override
    public String LANGUAGE_CODE() {
        return "ENG";
    }

    @Override
    public String LANGUAGE_NAME() {
        return "English:flag_gb:";
    }
    @Override
    public String LANGUAGE_DESCRIPTION() {
        return "Please use the `~lang` `ENG` command.";
    }

    @Override
    public String HELP_INFO() {
        return "GomokuBot / Help";
    }
    @Override
    public String HELP_DESCRIPTION() {
        return "GomokuBot is an open-source artificial intelligence Discord Bot that provides Gomoku(Omok) feature in Discord. The collected data is used for training reinforcement learning models.";
    }
    @Override
    public String HELP_DEV() {
        return "Developer";
    }
    @Override
    public String HELP_GIT() {
        return "Git Repository";
    }
    @Override
    public String HELP_VERSION() {
        return "Version";
    }
    @Override
    public String HELP_SUPPORT() {
        return "Support Channel";
    }
    @Override
    public String HELP_INVITE_LINK() {
        return "Invite Link";
    }

    @Override
    public String HELP_CMD_INFO() {
        return "GomokuBot / Command";
    }
    @Override
    public String HELP_CMD_HELP() {
        return "`~help` Get help";
    }
    @Override
    public String HELP_CMD_RANK() {
        return "`~rank` Show the ranking from 1st to 10th";
    }
    @Override
    public String HELP_CMD_LANG(String langList) {
        return "`~lang` " + langList + " Change the language setting used on this server. Ex) `~lang` `ENG`";
    }
    @Override
    public String HELP_CMD_SKIN() {
        return "`~skin` `A` `B` `C` Change the Gomoku-canvas style setting used on this server. Ex) `~skin` `A`";
    }
    @Override
    public String HELP_CMD_PVE() {
        return "`~start` Start the game with A.I.";
    }
    @Override
    public String HELP_CMD_PVP() {
        return "`~start` `@mention` Start the game with the mentioned player. Ex) `~start` `@player`";
    }
    @Override
    public String HELP_CMD_RESIGN() {
        return "`~resign` Surrender the current game.";
    }

    @Override
    public String SKIN_INFO() {
        return "GomokuBot / Style";
    }
    @Override
    public String SKIN_DESCRIPTION() {
        return "The default Gomoku canvas (Style A) may not display properly. Choose one of the three styles available and set the style to use on this server.";
    }
    @Override
    public String SKIN_CMD_INFO(String style) {
        return "Enter ``~skin`` ``" + style + "`` to use this style.";
    }
    @Override
    public String SKIN_CHANGE_ERROR() {
        return "There is an error in the style specification.";
    }
    @Override
    public String SKIN_CHANGE_SUCCESS(String style) {
        return "Style setting has been change to ``" + style + "`` !";
    }

    @Override
    public String RANK_INFO() {
        return "GomokuBot / Ranking";
    }
    @Override
    public String RANK_DESCRIPTION() {
        return "Ranked 1st to 10th.";
    }
    @Override
    public String RANK_WIN() {
        return "Victory";
    }
    @Override
    public String RANK_LOSE() {
        return "Defeat";
    }

    @Override
    public String LANG_CHANGE_ERROR() {
        return "There is an error in the language specification.";
    }
    @Override
    public String LANG_SUCCESS() {
        return "Language setting has been changed to English:flag_gb:!";
    }

    @Override
    public String GAME_NOT_FOUND(String nameTag) {
        return nameTag + ", could not find any games in progress. Please start the game with `~start` command!";
    }
    @Override
    public String GAME_CREATE_FAIL(String nameTag) {
        return nameTag + ", Game creation failed. Please finish the game in progress. :thinking:";
    }
    @Override
    public String GAME_SYNTAX_FAIL(String nameTag) {
        return nameTag + ", that's invalid command. Please write in the format of . `~s` `alphabet` `number` :thinking:";
    }
    @Override
    public String GAME_ALREADY_IN(String nameTag) {
        return nameTag + ", there is already a stone. :thinking:";
    }

    @Override
    public String GAME_CREATE_INFO(String playerName, String targetName, String fAttack) {
        return "The match between`" + playerName + "` vs `" + targetName + "` has begun! Attack first is `" + fAttack + "`.";
    }
    @Override
    public String GAME_CMD_INFO() {
        return "Please place the Stone by `~s` `alphabet` `number` format. Ex) `~s` `h` `8`";
    }

    @Override
    public String GAME_NEXT_TURN(String curName, String prvName, String lastPos) {
        return "`" + curName + "`, please place the next Stone. `" + prvName + "` was placed on " + lastPos;
    }

    @Override
    public String GAME_PVP_TURN(String turnName) {
        return "It's `"+ turnName + "`s turn now. Please wait for `"+ turnName +"`'s next stone. :thinking:";
    }
    @Override
    public String GAME_PVP_WIN(String winName, String loseName, String lastPos) {
        return "`" + winName + "` wins by `" + loseName + "` placing Stone on " + lastPos + "!";
    }
    @Override
    public String GAME_PVP_RESIGN(String winName, String loseName) {
        return "`" + winName + "` wins by `" + loseName + "` declaring surrender!";
    }

    @Override
    public String GAME_PVP_INFO(String winName, String loseName, int winCount, int loseCount) {
        return "`" + winName + "` vs `" + loseName + "` have been updated to `" + winCount + " : " + loseCount + "`.";
    }

    @Override
    public String GAME_PVE_WIN(String lastPos) {
        return "You beat AI by placing Stone on " + lastPos + ". Congratulations! :tada:";
    }
    @Override
    public String GAME_PVE_LOSE(String lastPos) {
        return "You have been defeated by AI placing on " + lastPos + ".";
    }
    @Override
    public String GAME_PVE_RESIGN() {
        return "You have been defeated by declaring surrender.";
    }

    @Override
    public String GAME_PVE_INFO(String playerName, int winCount, int loseCount, int rank) {
        return "Your entire AI has been updated to `" + winCount + " : " + loseCount + "`. " + playerName + " is currently ranked " + rank + " above.";
    }

    @Override
    public String GAME_FULL() {
        return "There was no more space for the stones, so it was a draw.";
    }

    @Override
    public String GAME_ARCHIVED(String messageLink) {
        return ":tada: You showed a great game! The game record has been shared to the official channel. - Click link to check it out.\n"+messageLink;
    }

    @Override
    public String BOARD_INP() {
        return "in Processing";
    }
    @Override
    public String BOARD_FINISH() {
        return "Finished";
    }
    @Override
    public String BOARD_TURNS() {
        return "Turn Progress";
    }
    @Override
    public String BOARD_TURN() {
        return "Turns";
    }
    @Override
    public String BOARD_LOCATION() {
        return "Latest Location";
    }

}
