package junghyun.discord.ui.languages;

public interface LanguageInterface {

    String[] TARGET_REGION();

    String LANGUAGE_CODE();

    String LANGUAGE_NAME();
    String LANGUAGE_DESCRIPTION();

    String HELP_INFO();
    String HELP_DESCRIPTION();
    String HELP_DEV();
    String HELP_GIT();
    String HELP_VERSION();
    String HELP_SUPPORT();
    String HELP_INVITE_LINK();

    String HELP_CMD_INFO();
    String HELP_CMD_HELP();
    String HELP_CMD_LANG(String langList);
    String HELP_CMD_SKIN();
    String HELP_CMD_RANK();
    String HELP_CMD_PVE();
    String HELP_CMD_PVP();
    String HELP_CMD_RESIGN();

    String SKIN_INFO();
    String SKIN_DESCRIPTION();
    String SKIN_CMD_INFO(String style);
    String SKIN_CHANGE_ERROR();
    String SKIN_CHANGE_SUCCESS(String style);

    String RANK_INFO();
    String RANK_DESCRIPTION();
    String RANK_WIN();
    String RANK_LOSE();

    String LANG_CHANGE_ERROR();
    String LANG_SUCCESS();

    String GAME_NOT_FOUND(String nameTag);
    String GAME_CREATE_FAIL(String nameTag);
    String GAME_SYNTAX_FAIL(String nameTag);
    String GAME_ALREADY_IN(String nameTag);

    String GAME_CREATE_INFO(String playerName, String targetName, String fAttack);
    String GAME_CMD_INFO();

    String GAME_NEXT_TURN(String curName, String prvName, String lastPos);

    String GAME_PVP_TURN(String turnName);
    String GAME_PVP_WIN(String winName, String loseName, String lastPos);
    String GAME_PVP_RESIGN(String winName, String loseName);
    String GAME_PVP_INFO(String winName, String loseName, int winCount, int loseCount);

    String GAME_PVE_WIN(String lastPos);
    String GAME_PVE_LOSE(String lastPos);
    String GAME_PVE_RESIGN();
    String GAME_PVE_INFO(String playerName, int winCount, int loseCount, int rank);

    String GAME_FULL();

    String GAME_ARCHIVED(String messageLink);

    String BOARD_INP();
    String BOARD_FINISH();
    String BOARD_TURNS();
    String BOARD_TURN();
    String BOARD_LOCATION();

}
