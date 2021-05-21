package junghyun.discord.ui.languages;

public class LanguageKOR extends LanguageENG {

    @Override
    public String[] TARGET_REGION() {
        return new String[]{
                "south-korea",
                "vip-south-korea"
        };
    }

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
        return "`~lang` `KOR` 명령어를 사용해주세요.";
    }

    @Override
    public String HELP_INFO() {
        return "GomokuBot / 도움말";
    }
    @Override
    public String HELP_DESCRIPTION() {
        return "GomokuBot 은 디스코드에서 오목 기능을 제공하는 오픈소스 디스코드 봇 입니다. 수집된 기보 데이터는 강화학습 모델 훈련에 사용됩니다.";
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
    public String HELP_INVITE_LINK() {
        return "초대 링크";
    }

    @Override
    public String HELP_CMD_INFO() {
        return "GomokuBot / 명령어";
    }
    @Override
    public String HELP_CMD_HELP() {
        return "`~help` 도움말을 출력합니다.";
    }
    @Override
    public String HELP_CMD_LANG(String langList) {
        return "`~lang` " + langList + "이 서버에서 사용되는 언어 설정을 바꿉니다. Ex) `~lang` `ENG`";
    }
    @Override
    public String HELP_CMD_SKIN() {
        return "`~skin` `A` `B` `C` 이 서버에서 사용되는 오목판의 스타일 설정을 바꿉니다. Ex) `~skin` `A`";
    }
    @Override
    public String HELP_CMD_PVE() {
        return "`~start` 인공지능과의 게임을 시작합니다.";
    }
    @Override
    public String HELP_CMD_PVP() {
        return "`~start` `@언급` 언급된 플레이어와의 게임을 시작합니다. Ex) `~start` `@player`";
    }
    @Override
    public String HELP_CMD_RESIGN() {
        return "`~resign` 현재 진행되고 있는 게임을 포기합니다.";
    }

    @Override
    public String SKIN_INFO() {
        return "GomokuBot / 스타일";
    }
    @Override
    public String SKIN_DESCRIPTION() {
        return "GomokuBot에서 제공하는 기본 오목판(``Style A``)이 제대로 표시되지 않을 수 있습니다. 준비된 세 가지 스타일 중 하나를 선택해 이 서버에서 사용할 스타일을 설정해 주세요.";
    }
    @Override
    public String SKIN_CMD_INFO(String style) {
        return "이 스타일을 사용 하려면 ``~skin`` ``" + style + "`` 명령어를 입력해주세요.";
    }
    @Override
    public String SKIN_CHANGE_ERROR() {
        return "스타일 지정에 오류가 있습니다.";
    }
    @Override
    public String SKIN_CHANGE_SUCCESS(String style) {
        return "스타일 설정이 스타일 ``" + style + "``로 바뀌었습니다!";
    }

    @Override
    public String RANK_INFO() {
        return "GomokuBot / 순위";
    }
    @Override
    public String HELP_CMD_RANK() {
        return "`~rank` 1위부터 10위까지의 순위를 출력합니다.";
    }
    @Override
    public String RANK_DESCRIPTION() {
        return "1위부터 10위까지의 순위입니다.";
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
        return "언어 지정에 오류가 있습니다.";
    }
    @Override
    public String LANG_SUCCESS() {
        return "언어 설정이 한국어:flag_kr:로 바뀌었습니다!";
    }

    @Override
    public String GAME_NOT_FOUND(String nameTag) {
        return nameTag + "님, 진행 중인 게임을 찾을 수 없습니다. `~start` 명령어로 게임을 시작해 주세요!";
    }
    @Override
    public String GAME_CREATE_FAIL(String nameTag) {
        return nameTag + "님, 게임 생성에 실패했습니다. 진행중에 있는 게임을 마무리해 주세요. :thinking:";
    }
    @Override
    public String GAME_SYNTAX_FAIL(String nameTag) {
        return nameTag + "님, 그것은 잘못된 명령어입니다. `~s 알파벳 숫자` 형식으로 적어주세요. :thinking:";
    }
    @Override
    public String GAME_ALREADY_IN(String nameTag) {
        return nameTag + "님, 그곳에는 이미 돌이 놓여 있습니다. :thinking:";
    }

    @Override
    public String GAME_CREATE_INFO(String playerName, String targetName, String fAttack) {
        return "`" + playerName + "`님과 `" + targetName + "`님과의 대결이 시작되었습니다! 선공은 `" + fAttack + "`님입니다.";
    }
    @Override
    public String GAME_CMD_INFO() {
        return " `~s` `알파벳` `숫자` 형식으로 돌을 놓아주세요. Ex) `~s` `h` `8`";
    }

    @Override
    public String GAME_NEXT_TURN(String curName, String prvName, String lastPos) {
        return "`" + curName + "`님, 다음 수를 놓아주세요. `" + prvName + "`는 " + lastPos + "에 놓았습니다.";
    }

    @Override
    public String GAME_PVP_TURN(String turnName) {
        return "지금은 `" + turnName + "`님의 차례입니다. `" + turnName + "`님의 다음 수를 기다려 주세요. :thinking:";
    }
    @Override
    public String GAME_PVP_WIN(String winName, String loseName, String lastPos) {
        return "`" + winName + "`님이 " + lastPos + "에 놓음으로써 `" + loseName + "`님에게 승리하였습니다!";
    }
    @Override
    public String GAME_PVP_RESIGN(String winName, String loseName) {
        return "`" + loseName + "`님이 항복을 선언 함으로써 `" + winName + "`님이 승리하였습니다!";
    }

    @Override
    public String GAME_PVP_INFO(String winName, String loseName, int winCount, int loseCount) {
        return winName + "님과 " + loseName + "님의 전적이 `" + winCount + " : " + loseCount + "`로 업데이트되었습니다.";
    }

    @Override
    public String GAME_PVE_WIN(String lastPos) {
        return lastPos + "에 놓음으로써 A.I.에게 승리하셨습니다. 축하합니다!";
    }
    @Override
    public String GAME_PVE_LOSE(String lastPos) {
        return "A.I. 가 " + lastPos + "에 놓음으로써 패배하셨습니다.";
    }
    @Override
    public String GAME_PVE_RESIGN() {
        return "항복을 선언 함으로써 A.I.에게 패배하였습니다.";
    }

    @Override
    public String GAME_PVE_INFO(String playerName, int winCount, int loseCount, int rank) {
        return "A.I. 상대 전적이 `" + winCount + " : " + loseCount + "`로 업데이트되었습니다. 현재 " + playerName + "님의 순위는 " + rank + "위입니다.";
    }

    @Override
    public String GAME_FULL() {
        return "더는 돌을 놓을 자리가 없어 무승부 처리되었습니다.";
    }

    @Override
    public String GAME_ARCHIVED(String messageLink) {
        return ":tada: 멋진 게임을 보여 주셨습니다! 게임 결과가 공식 채널에 공유되었습니다. - 링크를 클릭해서 확인해보세요."+messageLink;
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
