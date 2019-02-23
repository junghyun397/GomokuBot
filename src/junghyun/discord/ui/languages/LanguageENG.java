package junghyun.discord.ui.languages;

public class LanguageENG implements LanguageInterface {

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
        return "GomokuBot is an Open-Source Discord Bot that allows you to enjoy Gomoku/Omok in Discord. Collected game data is used for Reinforcement Learning Model training. :)";
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
    public String HELP_CMD_INFO() {
        return "GomokuBot / Command";
    }
    @Override
    public String HELP_CMD_HELP() {
        return "`~help` Get help";
    }
    @Override
    public String HELP_CMD_LANG(String langList) {
        return "`~lang` " + langList + " Change the language setting used on this server. Ex) `~lang` `ENG`";
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
        return "`~resign` Surrenders the current game.";
    }

    @Override
    public String RANK_INFO() {
        return "GomokuBot / Ranking";
    }
    @Override
    public String RANK_DESCRIPTION() {
        return "It is ranked from 1 to 10. :D";
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
        return nameTag + ", could not find any games in progress Please start the game with `~start` command!";
    }
    @Override
    public String GAME_CREATE_FAIL(String nameTag) {
        return nameTag + ", Game creation failed. Please finish the game you are enjoying. :thinking:";
    }
    @Override
    public String GAME_SYNTAX_FAIL(String nameTag) {
        return nameTag + ", that's an invalid command. Please write in the format of . `~s` `alphabet` `number` :thinking:";
    }
    @Override
    public String GAME_ALREADY_IN(String nameTag) {
        return nameTag + ", there are stones already there. :thinking:";
    }

    @Override
    public String GAME_CREATE_INFO(String playerName, String targetName, String fAttack) {
        return "The match between`" + playerName + "` and `" + targetName + "` has begun! The First Attacker is `" + fAttack + "`.";
    }
    @Override
    public String GAME_CMD_INFO() {
        return "Please release the stone by `~s` `alphabet` `number` format. Ex) `~s` h` `8`";
    }

    @Override
    public String GAME_NEXT_TURN(String curName, String prvName, String lastPos) {
        return "`" + curName + "`, please release the next Stone. `" + prvName + "` was placed on " + lastPos;
    }

    @Override
    public String GAME_PVP_TURN(String turnName) {
        return "It is now `"+ turnName + "`s turn. Please wait for the next Stone of `"+ turnName +"`. :thinking:";
    }
    @Override
    public String GAME_PVP_WIN(String winName, String loseName, String lastPos) {
        return "`" + winName + "` wins by `" + loseName + "`  putting it on " + lastPos + "!";
    }
    @Override
    public String GAME_PVP_RESIGN(String winName, String loseName) {
        return "`" + winName + "` wins by `" + loseName + "` declaring surrender!";
    }

    @Override
    public String GAME_PVP_INFO(String winName, String loseName, int winCount, int loseCount) {
        return "`" + winName + "` and `" + loseName + "` have been updated to `" + winCount + " : " + loseCount + "`.";
    }

    @Override
    public String GAME_PVE_WIN(String lastPos) {
        return "You won the AI by putting it on " + lastPos + ". Congratulations!";
    }
    @Override
    public String GAME_PVE_LOSE(String lastPos) {
        return "You have been defeated by placing AI on " + lastPos + ".";
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
        return "There was no more room for the stones, so it was a draw.";
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
