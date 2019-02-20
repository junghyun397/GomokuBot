package junghyun.discord.ui.languages;

public class LanguagePRK implements LanguageInterface {

    @Override
    public String LANGUAGE_CODE() {
        return "PRK";
    }

    @Override
    public String LANGUAGE_NAME() {
        return "조선말:flag_kp:";
    }
    @Override
    public String LANGUAGE_DESCRIPTION() {
        return "`~lang` `PRK` 시킴말을 사용 하라우.";
    }

    @Override
    public String HELP_INFO() {
        return "GomokuBot / 도움말";
    }
    @Override
    public String HELP_DESCRIPTION() {
        return "GomokuBot은 불협화음에서 오목 오락을 즐길 수 있게 하는 열린문서 Discord Bot 이라우. 모아진 기보 내용은 강화학습 형태 훈련에 사용됩네다.";
    }
    @Override
    public String HELP_DEV() {
        return "주체자";
    }
    @Override
    public String HELP_GIT() {
        return "직결 저장소";
    }
    @Override
    public String HELP_VERSION() {
        return "판올림";
    }
    @Override
    public String HELP_SUPPORT() {
        return "지원 채널";
    }

    @Override
    public String HELP_CMD_INFO() {
        return "GomokuBot / 시킴말";
    }
    @Override
    public String HELP_CMD_HELP() {
        return "`~help` 시킴말을 알려 드리겠소.";
    }
    @Override
    public String HELP_CMD_LANG(String langList) {
        return "`~lang` " + langList + "이 봉사기에서 사용되는 말씨 설정을 바꿉네다. Ex) `~lang` `PRK`";
    }
    @Override
    public String HELP_CMD_PVE() {
        return "`~start` 콤퓨타와의 놀음을 시작합네다.";
    }
    @Override
    public String HELP_CMD_PVP() {
        return "`~start` `@언급` 언급된 놀음꾼과의 놀음을 시작 합네다. Ex) `~start` `@player`";
    }
    @Override
    public String HELP_CMD_RESIGN() {
        return "`~resign` 현재 진행하고 있는 놀음을 포기합네다.";
    }

    @Override
    public String RANK_INFO() {
        return "GomokuBot / 순위";
    }
    @Override
    public String RANK_DESCRIPTION() {
        return "1위부터 10위까지의 순위 입네다.";
    }
    @Override
    public String RANK_WIN() {
        return "공산";
    }
    @Override
    public String RANK_LOSE() {
        return "자본";
    }

    @Override
    public String LANG_CHANGE_ERROR() {
        return "언어 지정에 문제가 있습네다.";
    }
    @Override
    public String LANG_SUCCESS() {
        return "언어 설정이 조선말:flag_kp:로 바뀌었습네다!";
    }

    @Override
    public String GAME_NOT_FOUND(String nameTag) {
        return nameTag + "동무, 진행중인 놀음을 찾을 수 없습니다. `~start`시킴말로 놀음을 시작 해주시오!";
    }
    @Override
    public String GAME_CREATE_FAIL(String nameTag) {
        return nameTag + "동무, 놀음 만들기에 실패 했습네다. 즐기고 있던 놀음을 마무리 해주시우. :thinking:";
    }
    @Override
    public String GAME_SYNTAX_FAIL(String nameTag) {
        return nameTag + "동무, 그건 잘못된 시킴말 입네다. `~s 미국말 숫자` 형식으로 적어주시우. :thinking:";
    }
    @Override
    public String GAME_ALREADY_IN(String nameTag) {
        return nameTag + "동무, 그곳에는 이미 돌이 놓여 있습네다. :thinking:";
    }

    @Override
    public String GAME_CREATE_INFO(String playerName, String targetName, String fAttack) {
        return "`" + playerName + "`동무과 `" + targetName + "`동무과의 싸움이 시작 되었습네다! 선공은 `" + fAttack + "`동무 입네다.";
    }
    @Override
    public String GAME_CMD_INFO() {
        return " `~s` `미국말` `숫자` 형식으로 돌을 놓아주시우. Ex) `~s` `h` `8`";
    }

    @Override
    public String GAME_NEXT_TURN(String curName, String prvName, String lastPos) {
        return "`" + curName + "`동무, 다음 수를 놓아주시우. `" + prvName + "`동무는 " + lastPos + "에 놓았습네다.";
    }

    @Override
    public String GAME_PVP_TURN(String turnName) {
        return "지금은 `" + turnName + "`동무의 차례입니다. `" + turnName + "`동무의 다음 수를 기다려 주시우. :thinking:";
    }
    @Override
    public String GAME_PVP_WIN(String winName, String loseName, String lastPos) {
        return "`" + winName + "`동무가 " + lastPos + "에 놓음으로서 `" + loseName + "`동무에게 승리 하였습네다!";
    }
    @Override
    public String GAME_PVP_RESIGN(String winName, String loseName) {
        return "`" + loseName + "`동무가 항복을 선언 함으로서 `" + winName + "`동무가 승리 하였습네다!";
    }

    @Override
    public String GAME_PVP_INFO(String winName, String loseName, int winCount, int loseCount) {
        return winName + "동무과 " + loseName + "동무의 전적이 `" + winCount + " : " + loseCount + "`로 바뀜질 되었습네다.";
    }

    @Override
    public String GAME_PVE_WIN(String lastPos) {
        return lastPos + "에 놓음으로서 콤퓨타에게 승리 하였습네다. 축하합네다!";
    }
    @Override
    public String GAME_PVE_LOSE(String lastPos) {
        return "AI 가 " + lastPos + "에 놓음으로서 패배 하였습네다.";
    }
    @Override
    public String GAME_PVE_RESIGN() {
        return "항복을 선언 함으로서 콤퓨타에게 패배 하였습네다.";
    }

    @Override
    public String GAME_PVE_INFO(String playerName, int winCount, int loseCount, int rank) {
        return "콤퓨타 상대 전적이 `" + winCount + " : " + loseCount + "`로 바뀜질 되었습네다. 현재 " + playerName + "동무의 순위는 " + rank + "위 입네다.";
    }

    @Override
    public String GAME_FULL() {
        return "더이상 돌을 넣을 자리가 없어 무승부 처리 되었습네다.";
    }

    @Override
    public String BOARD_INP() {
        return "진행중";
    }
    @Override
    public String BOARD_FINISH() {
        return "종료됨";
    }
    @Override
    public String BOARD_TURNS() {
        return "순서 진행도";
    }
    @Override
    public String BOARD_TURN() {
        return "순서";
    }
    @Override
    public String BOARD_LOCATION() {
        return "마지막 위치";
    }

}
