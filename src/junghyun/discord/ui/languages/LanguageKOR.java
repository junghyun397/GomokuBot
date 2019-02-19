package junghyun.discord.ui.languages;

public class LanguageKOR implements LanguageInterface {

    @Override
    public String LANGUAGE_CODE() {
        return "KOR";
    }

    @Override
    public String LANGUAGE_NAME() {
        return "한국어:flag_kr:";
    }
    @Override
    public String LANGUAGE_DESCRIPTION() {
        return "`~lang` `KOR` 명령어를 사용 해주세요.";
    }

    @Override
    public String HELP_INFO() {
        return "GomokuBot / 도움말";
    }
    @Override
    public String HELP_DESCRIPTION() {
        return "GomokuBot 은 Discord 에서 오목을 즐길 수 있게 해주는 오픈소스 Discord Bot 입니다. 수집된 기보 데이터는 강화학습 모델 훈련에 사용됩니다. :)";
    }
    @Override
    public String HELP_DEV() {
        return "개발자";
    }
    @Override
    public String HELP_GIT() {
        return "Git 저장소";
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
        return "GomokuBot / 명령어";
    }
    @Override
    public String HELP_CMD_HELP() {
        return "`~help` 도움말을 알려 드립니다.";
    }
    @Override
    public String HELP_CMD_LANG(String langList) {
        return "`~lang` " + langList + "이 서버에서 사용되는 언어 설정을 바꿉니다. Ex) `~lang` `ENG`";
    }
    @Override
    public String HELP_CMD_PVE() {
        return "`~start` A.I 와의 게임을 시작합니다.";
    }
    @Override
    public String HELP_CMD_PVP() {
        return "`~start` `@언급` 언급된 플레이어와의 게임을 시작 합니다. Ex) `~start` `@player`";
    }
    @Override
    public String HELP_CMD_RESIGN() {
        return "`~resign` 현재 진행하고 있는 게임을 포기합니다.";
    }

    @Override
    public String RANK_INFO() {
        return "GomokuBot / 순위";
    }
    @Override
    public String RANK_DESCRIPTION() {
        return "1위부터 10위까지의 순위 입니다. :D";
    }
    @Override
    public String RANK_WIN() {
        return "승리";
    }
    @Override
    public String RANK_LOSE() {
        return "패배";
    }

    @Override
    public String LANG_CHANGE_ERROR() {
        return "언어 지정에 오류가 있습니다!";
    }
    @Override
    public String LANG_SUCCESS() {
        return "언어 설정이 한국어:flag_kr:로 바뀌었습니다!";
    }

    @Override
    public String GAME_NOT_FOUND(String nameTag) {
        return nameTag + "님, 진행중인 게임을 찾을 수 없습니다. `~start`명령어로 게임을 시작 해주세요!";
    }
    @Override
    public String GAME_CREATE_FAIL(String nameTag) {
        return nameTag + "님, 게임 생성에 실패 했습니다. 즐기고 계신 게임을 마무리 해주세요. :thinking:";
    }
    @Override
    public String GAME_SYNTAX_FAIL(String nameTag) {
        return nameTag + "님, 그건 잘못된 명령어 입니다. `~s 알파벳 숫자` 형식으로 적어주세요. :thinking:";
    }
    @Override
    public String GAME_ALREADY_IN(String nameTag) {
        return nameTag + "님, 그곳에는 이미 돌이 놓여 있습니다. :thinking:";
    }

    @Override
    public String GAME_CREATE_INFO(String playerName, String targetName, String fAttack) {
        return "`" + playerName + "`님과 `" + targetName + "`님과의 대결이 시작 되었습니다! 선공은 `" + fAttack + "`님 입니다.";
    }
    @Override
    public String GAME_CMD_INFO() {
        return " `~s` `알파벳` `숫자` 형식으로 돌을 놓아주세요. Ex) `~s` `h` `8`";
    }

    @Override
    public String GAME_NEXT_TURN(String curName, String prvName, String lastPos) {
        return curName + "님, 다음 수를 놓아주세요. `" + prvName + "`는 " + lastPos + "에 놓았습니다.";
    }

    @Override
    public String GAME_PVP_TURN(String turnName) {
        return "지금은 `" + turnName + "`님 의 차례입니다. `" + turnName + "`님의 다음 수를 기다려 주세요. :thinking:";
    }
    @Override
    public String GAME_PVP_WIN(String winName, String loseName, String lastPos) {
        return "`" + winName + "`님이 " + lastPos + "에 놓음으로서 `" + loseName + "`님에게 승리 하였습니다!";
    }
    @Override
    public String GAME_PVP_RESIGN(String winName, String loseName) {
        return "`" + loseName + "`님이 항복을 선언 함으로서 `" + winName + "`님이 승리 하였습니다!";
    }

    @Override
    public String GAME_PVP_INFO(String winName, String loseName, int winCount, int loseCount) {
        return winName + "님과 " + loseName + "님의 전적이 `" + winCount + " : " + loseCount + "`로 업데이트 되었습니다.";
    }

    @Override
    public String GAME_PVE_WIN(String lastPos) {
        return lastPos + "에 놓음으로서 AI에게 승리 하셨습니다. 축하합니다!";
    }
    @Override
    public String GAME_PVE_LOSE(String lastPos) {
        return "AI 가 " + lastPos + "에 놓음으로서 패배 하셨습니다.";
    }
    @Override
    public String GAME_PVE_RESIGN() {
        return "항복을 선언 함으로서 AI에게 패배 하였습니다.";
    }

    @Override
    public String GAME_PVE_INFO(String playerName, int winCount, int loseCount, int rank) {
        return "AI 상대 전적이 `" + winCount + " : " + loseCount + "`로 업데이트 되었습니다. 현재 " + playerName + "님의 순위는 " + rank + "위 입니다.";
    }

    @Override
    public String GAME_FULL() {
        return "더이상 돌을 넣을 자리가 없어 무승부 처리 되었습니다.";
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
        return "턴 진행도";
    }
    @Override
    public String BOARD_TURN() {
        return "턴";
    }
    @Override
    public String BOARD_LOCATION() {
        return "최근 착수 위치";
    }

}
